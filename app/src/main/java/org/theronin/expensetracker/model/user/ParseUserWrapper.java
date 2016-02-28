package org.theronin.expensetracker.model.user;

import android.content.Context;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class ParseUserWrapper extends User {

    private ParseUser parseUser;

    public ParseUserWrapper(Context context) {
        this(context, new ParseUser());
    }

    public ParseUserWrapper(Context context, ParseUser parseUser) {
        super(context);
        this.parseUser = parseUser;
    }

    @Override
    public String getId() {
        return parseUser.getObjectId();
    }

    @Override
    public String getEmail() {
        return parseUser.getEmail();
    }

    @Override
    public boolean signedIn() {
        return ParseUser.getCurrentUser() != null;
    }

    @Override
    public boolean canSync() {
        return this.parseUser != null;
    }

    @Override
    public void requestChangePassword(final Callback callback) {
        ParseUser.requestPasswordResetInBackground(getEmail(), new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(e);
                }
            }
        });
    }

    @Override
    public void createAccount(final Callback callback) throws NoInternetException {
        super.createAccount(callback);
        parseUser.setEmail(email);
        parseUser.setUsername(email);
        parseUser.setPassword(password);
        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(e);
                }
            }
        });
    }

    @Override
    public void signIn(final Callback callback) throws NoInternetException {
        super.signIn(callback);
        ParseUser.logInInBackground(
                email,
                password,
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(e);
                        }
                    }
                });
    }
}
