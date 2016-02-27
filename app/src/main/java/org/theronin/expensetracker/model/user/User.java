package org.theronin.expensetracker.model.user;

//TODO custom exceptions here to flag the UI
public abstract class User {

    protected String email;
    protected String password;
    protected boolean passwordConfirmed;

    private Callback callback;

    public abstract String getId();

    public abstract String getEmail();

    public abstract boolean signedIn();

    public abstract boolean canSync();

    public User setEmail(String email) {
        //TODO more email validation
        this.email = email;
        return this;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public User setNewPassword(String password, String confirmPassword) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("New passwords need to be at least 8 characters");
        }
        //TODO more password validation
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        this.password = password;
        passwordConfirmed = true;
        return this;
    }

    public void createAccount(Callback callback) {
        this.callback = callback;
        if (email == null || password == null) {
            callback.onFailure(new IllegalStateException("Email or password not set!"));
            return;
        }
        if (!passwordConfirmed) {
            callback.onFailure(new IllegalStateException("To create an account, ensure that the password has been confirmed"));
            return;
        }
    }

    public void signIn(Callback callback) {
        this.callback = callback;
        if (email == null || password == null) {
            callback.onFailure(new IllegalStateException("Email or password not set!"));
        }
    }

    public interface Callback {
        void onSuccess();

        void onFailure(Exception e);
    }
}
