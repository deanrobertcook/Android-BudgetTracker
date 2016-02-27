package org.theronin.expensetracker.model.user;

import android.content.Context;

import com.parse.ParseUser;

import org.theronin.expensetracker.utils.Prefs;

public class UserManager {

    public static boolean signedIn(Context context) {
        return getUser(context).signedIn();
    }

    public static User getUser(Context context) {
        if (ParseUser.getCurrentUser() != null) {
            return new ParseUserWrapper(ParseUser.getCurrentUser());
        } else if (Prefs.loggedInAsDefaultUser(context)) {
            return new DefaultUser(context);
        }
        return new NullUser();
    }
}
