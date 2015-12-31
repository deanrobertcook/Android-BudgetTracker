package org.theronin.expensetracker.data.sync;

import com.parse.ParseException;
import com.parse.ParseObject;

import org.theronin.expensetracker.data.Contract;
import org.theronin.expensetracker.model.Entity;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ParseRemoteSync implements RemoteSync {
    @Override
    public void addEntitiesToRemote(Collection<? extends Entity> entities, Callback callback) {
        Map<Entity, ParseObject> toSync = createObjectMap(entities);
        try {
            ParseObject.saveAll(new ArrayList<>(toSync.values()));
            for (Entity entity : entities) {
                entity.setGlobalId(toSync.get(entity).getObjectId());
                entity.setSyncState(SyncState.SYNCED);
            }
            callback.onSuccess();
        } catch (ParseException e) {
            callback.onFail(e);
        }
    }

    @Override
    public void updateEntitiesOnRemote(Collection<? extends Entity> entities, Callback callback) {
        addEntitiesToRemote(entities, callback);
    }

    @Override
    public void deleteEntitiesFromRemote(Collection<? extends Entity> entities, Callback callback) {
        Map<Entity, ParseObject> toSync = createObjectMap(entities);
        try {
            ParseObject.deleteAll(new ArrayList<>(toSync.values()));
            for (Entity entity : entities) {
                entity.setSyncState(SyncState.DELETE_SYNCED);
            }
            callback.onSuccess();
        } catch (ParseException e) {
            callback.onFail(e);
        }
    }

    private Map<Entity, ParseObject> createObjectMap(Collection<? extends Entity> entities) {
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
