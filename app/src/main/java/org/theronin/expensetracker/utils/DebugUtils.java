package org.theronin.expensetracker.utils;

import org.theronin.expensetracker.model.Entity;

import java.util.Collection;

import timber.log.Timber;

public class DebugUtils {
    public static <T extends Entity> void printList(String message, Collection<T> entities) {
        Timber.v(message + " size: " + entities.size());
        for (T entity : entities) {
            Timber.v(entity.toString());
        }
    }

    public static void printListString(String message, Collection<String> entities) {
        Timber.v(message + " size: " + entities.size());
        for (String string : entities) {
            Timber.v(string);
        }
    }
}
