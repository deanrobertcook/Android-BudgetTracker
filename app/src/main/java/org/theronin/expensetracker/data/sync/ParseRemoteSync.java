package org.theronin.expensetracker.data.sync;

import com.parse.ParseObject;

import org.theronin.expensetracker.data.Contract;
import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseRemoteSync extends RemoteSync {

    /**
     * Used to map Entities to their created parse objects so we can later retrieve the globalId
     * for each recently uploaded Entity in getObjectId()
     */
    Map<Entity, ParseObject> currentSyncMap;

    @Override
    protected void bulkAddOperation(List<? extends Entity> entities) throws Exception {
        currentSyncMap = createEntityToParseObjectMap(entities);
        ParseObject.saveAll(new ArrayList<>(currentSyncMap.values()));
    }

    @Override
    protected String getObjectId(Entity entity) {
        return currentSyncMap.get(entity).getObjectId();
    }

    @Override
    protected void bulkDeleteOperation(List<? extends Entity> entities) throws Exception {
        ParseObject.deleteAll(createParseObjectsFromEntities(entities));
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
            object = ParseObject.createWithoutData(Contract.EntryView.VIEW_NAME, entry.getGlobalId());
        } else {
            object = new ParseObject(Contract.EntryView.VIEW_NAME);
        }
        object.put("amount", entry.amount);
        object.put("category", entry.category.name);
        object.put("currency", entry.currency.code);
        object.put("date", entry.utcDate);
        return object;
    }
}
