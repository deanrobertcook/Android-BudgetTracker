package org.theronin.expensetracker.model.user;

import android.content.Context;

import org.apache.commons.lang.NotImplementedException;
import org.theronin.expensetracker.utils.Prefs;

public class DefaultUser extends User {

    public static final String USER_NAME = "DEFAULT_USER";

    private final Context context;

    public DefaultUser(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getId() {
        return USER_NAME;
    }

    @Override
    public String getEmail() {
        return "Default User";
    }

    @Override
    public boolean signedIn() {
        return true;
    }

    @Override
    public boolean canSync() {
        return false;
    }

    @Override
    public User setEmail(String email) {
        throw new NotImplementedException("Can't set an email on the default user");
    }

    @Override
    public User setPassword(String password) {
        throw new NotImplementedException("Can't set a password on the default user");
    }

    @Override
    public User setNewPassword(String password, String confirmPassword) {
        throw new NotImplementedException("Can't set a password on the default user");
    }

    @Override
    public void createAccount(Callback callback) {
        Prefs.logInAsDefaultUser(context);
    }

    @Override
    public void signIn(Callback callback) {
        Prefs.logInAsDefaultUser(context);
    }
}
