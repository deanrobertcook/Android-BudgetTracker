package org.theronin.expensetracker.data.sync;

import org.theronin.expensetracker.model.Entity;

import java.util.List;

interface EntitySaver<T extends Entity> {
    void addEntitiesToRemote(List<T> entries);
    void updateEntitiesOnRemote(List<T> entries);
    void deleteEntitiesFromRemote(List<T> entries);
    void deleteEntitiesLocally(List<T> entries);
}
