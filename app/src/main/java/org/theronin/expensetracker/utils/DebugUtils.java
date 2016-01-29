package org.theronin.expensetracker.utils;

import org.theronin.expensetracker.model.Entity;

import java.util.List;

import timber.log.Timber;

public class DebugUtils {

    public static <T extends Entity> void printList(String tag, List<T> entities) {
        printList(tag, entities, 4);
    }

    public static <T extends Entity> void printList(String tag, List<T> entities, int max) {
        max = max > entities.size() ? entities.size() : max;
        Timber.tag(tag).d("Size: " + entities.size());
        for (int i = 0; i < max; i++) {
            Timber.tag(tag).d(entities.get(i).toString());
        }
        if (max < entities.size()) {
            Timber.tag(tag).d("... and %d more.", entities.size() - max);
        }
    }
}
