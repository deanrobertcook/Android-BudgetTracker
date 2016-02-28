package org.theronin.expensetracker.model.user;

import android.content.Context;

import com.parse.ParseUser;

import org.theronin.expensetracker.utils.Prefs;

import timber.log.Timber;

public class UserManager {

    private final Context context;

    private static UserManager instance;

    private User user;

    public static UserManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserManager(context);
        }
        return instance;
    }

    private UserManager (Context context) {
        this.context = context.getApplicationContext();
        this.user = new NullUser();
    }

    public static boolean signedIn(Context context) {
        return getUser(context).signedIn();
    }

    public static User getUser(Context context) {
        if (ParseUser.getCurrentUser() != null) {
            Timber.v("Returning parse user");
            return new ParseUserWrapper(context, ParseUser.getCurrentUser());
        } else if (Prefs.loggedInAsDefaultUser(context)) {
            Timber.v("Returning default user");
            return new DefaultUser(context);
        }
        Timber.v("Returning null user");
        return new NullUser();
    }
}
