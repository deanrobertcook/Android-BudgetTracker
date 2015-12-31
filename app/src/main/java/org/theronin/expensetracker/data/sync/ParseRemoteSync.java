package org.theronin.expensetracker.data.sync;

import com.parse.ParseException;
import com.parse.ParseObject;

import org.theronin.expensetracker.model.Entity;

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
            //TODO move this toParseObject() functionality here somehow
            toSync.put(entity, entity.toParseObject());
        }
        return toSync;
    }
}
