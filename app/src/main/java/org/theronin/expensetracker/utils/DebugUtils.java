package org.theronin.expensetracker.utils;

import org.theronin.expensetracker.model.Entity;

import java.util.List;

import timber.log.Timber;

public class DebugUtils {
    public static <T extends Entity> void printList(String message, List<T> entities) {
        Timber.v(message + " size: " + entities.size());
        for (T entity : entities) {
            Timber.v(entity.toString());
        }
    }
}
