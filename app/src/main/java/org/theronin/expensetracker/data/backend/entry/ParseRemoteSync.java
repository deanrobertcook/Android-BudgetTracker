package org.theronin.expensetracker.data.backend.entry;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseRemoteSync implements EntryRemote {

    /**
     * These constants correspond the the names of classes and their columns in the Parse.com storage
     */
    private static final String PARSE_OBJECT_UPDATED_AT = "updatedAt";
    private static final String ENTRY_CLASS_NAME = "Entry";
    private static final String ENTRY_CLASS_COL_AMOUNT = "amount";
    private static final String ENTRY_CLASS_COL_CATEGORY = "category";
    private static final String ENTRY_CLASS_COL_CURRENCY = "currency";
    private static final String ENTRY_CLASS_COL_DATE = "date";
    private static final String ENTRY_CLASS_COL_IS_DELETED = "isDeleted";

    /**
     * These constants correspond to the function name and keys for triggering push notifications
     */
    public static final String FUNCTION_SEND_CHANGE_PUSH = "sendChangePush";
    public static final String PARSE_DATA_INSTALLATION_ID_KEY = "installationId";

    /**
     * Used to map Entities to their created parse objects so we can later retrieve the globalId
     * for each recently uploaded Entity in getObjectId()
     */
    private Map<Entity, ParseObject> currentSyncMap;;

    @Override
    public void saveToRemote(List<? extends Entity> entities) throws Exception {
        currentSyncMap = createEntityToParseObjectMap(entities);
        try {
            ParseObject.saveAll(new ArrayList<>(currentSyncMap.values()));
            callPushToOtherDevices();
        } catch (ParseException e) {
            throw e;
        }
    }

    private void callPushToOtherDevices() {
        Map<String, Object> params = new HashMap<>();
        params.put(PARSE_DATA_INSTALLATION_ID_KEY, ParseInstallation.getCurrentInstallation().getInstallationId());
        ParseCloud.callFunctionInBackground(FUNCTION_SEND_CHANGE_PUSH, params);
    }

    @Override
    public String getObjectId(Entity entity) {
        return currentSyncMap.get(entity).getObjectId();
    }

    @Override
    public void deleteOnRemote(List<? extends Entity> entities) throws Exception {
        try {
            //Since I am using a soft-delete, I actually just want to update the objects
            ParseObject.saveAll(createParseObjectsFromEntities(entities));
            callPushToOtherDevices();
        } catch (ParseException e) {
            throw e;
        }
    }

    private List<ParseObject> createParseObjectsFromEntities(List<? extends Entity> entities) {
        List<ParseObject> objects = new ArrayList<>();
        for (Entity entity : entities) {
            objects.add(getParseObjectFrom(entity));
        }
        return objects;
    }

    private Map<Entity, ParseObject> createEntityToParseObjectMap(Collection<? extends Entity> entities) {
        Map<Entity, ParseObject> toSync = new HashMap<>();
        for (Entity entity : entities) {
            toSync.put(entity, getParseObjectFrom(entity));
        }
        return toSync;
    }

    private ParseObject getParseObjectFrom(Entity entity) {
        if (entity instanceof Entry) {
            return fromEntry((Entry) entity);
        }
        throw new IllegalArgumentException("Entity of type: " + entity.getClass() + " cannot currently " +
                " be converted to a ParseObject");
    }

    private ParseObject fromEntry(Entry entry) {
        ParseObject object;
        if (entry.hasGlobalId()) {
            object = ParseObject.createWithoutData(ENTRY_CLASS_NAME, entry.getGlobalId());
        } else {
            object = new ParseObject(ENTRY_CLASS_NAME);
        }
        object.put(ENTRY_CLASS_COL_AMOUNT, entry.amount);
        object.put(ENTRY_CLASS_COL_CATEGORY, entry.category.name);
        object.put(ENTRY_CLASS_COL_CURRENCY, entry.currency.code);
        object.put(ENTRY_CLASS_COL_DATE, entry.utcDate);

        if (entry.getSyncState() == SyncState.MARKED_AS_DELETED) {
            object.put(ENTRY_CLASS_COL_IS_DELETED, true);
        }
        return object;
    }

    @Override
    public void registerForPush() {
        ParsePush.subscribeInBackground(ParseUser.getCurrentUser().getObjectId());
    }

    @Override
    public List<Entry> pullFromRemote(long lastSync) throws Exception {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ENTRY_CLASS_NAME);
        query.setLimit(1000);
        if (lastSync == -1) { //first sync for device
            query.whereEqualTo(ENTRY_CLASS_COL_IS_DELETED, false);
        } else {
            Date date = new Date(lastSync);
            query.whereGreaterThan(PARSE_OBJECT_UPDATED_AT, date);
        }
        List<ParseObject> objects = query.find();
        List<Entry> entries = new ArrayList<>();
        for (ParseObject object : objects) {
            entries.add(createEntryFromParseObject(object));
        }
        return entries;
    }

    public Entry createEntryFromParseObject(ParseObject object) {
        String globalId = object.getObjectId();
        SyncState syncState = object.getBoolean(ENTRY_CLASS_COL_IS_DELETED) ? SyncState.DELETE_SYNCED : SyncState.SYNCED;
        long utcDate = object.getLong(ENTRY_CLASS_COL_DATE);
        long amount = object.getLong(ENTRY_CLASS_COL_AMOUNT);

        Category category = new Category(
                object.getString(ENTRY_CLASS_COL_CATEGORY)
        );

        Currency currency = new Currency(
                object.getString(ENTRY_CLASS_COL_CURRENCY)
        );

        return new Entry(globalId, syncState, utcDate, amount, category, currency);
    }
}
