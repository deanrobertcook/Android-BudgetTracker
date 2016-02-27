package org.theronin.expensetracker.model.user;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SignUpCallback;

public class ParseUserWrapper extends User {

    private ParseUser parseUser;

    public ParseUserWrapper() {
        this.parseUser = new ParseUser();
    }

    public ParseUserWrapper(ParseUser parseUser) {
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
    public void createAccount(final Callback callback) {
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
    public void signIn(final Callback callback) {
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
