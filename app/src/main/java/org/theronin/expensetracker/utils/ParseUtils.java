package org.theronin.expensetracker.utils;

import com.parse.ParseObject;

import org.theronin.expensetracker.model.Entity;

import java.util.ArrayList;
import java.util.List;

public class ParseUtils {
    public static List<ParseObject> fromEntities(List<Entity> entities) {
        List<ParseObject> objects = new ArrayList<>();
        for (Entity entity : entities) {
            objects.add(entity.toParseObject());
        }
        return objects;
    }
}
