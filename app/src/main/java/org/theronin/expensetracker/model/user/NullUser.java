package org.theronin.expensetracker.model.user;

public class NullUser extends User {
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public boolean signedIn() {
        return false;
    }

    @Override
    public boolean canSync() {
        return false;
    }

    @Override
    public void requestChangePassword(Callback callback) {
    }
}
