package org.theronin.expensetracker.model;

import java.io.Serializable;

public abstract class Entity implements Serializable {

    protected long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s: id: %d, ",
                getClass().getSimpleName(), id);
    }

    @Override
    public final int hashCode() {
        return 0;
    }
}
