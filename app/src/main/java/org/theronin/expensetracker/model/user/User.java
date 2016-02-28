package org.theronin.expensetracker.model.user;

import android.content.Context;
import android.util.Patterns;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.utils.NetworkUtils;

//TODO custom exceptions here to flag the UI
public abstract class User {

    public static final int PASSWORD_MIN_LENGTH = 8;
    protected String email;
    protected String password;
    protected boolean passwordConfirmed;

    private Callback callback;

    protected Context context;

    public abstract String getId();

    public abstract String getEmail();

    public abstract boolean signedIn();

    public abstract boolean canSync();

    public abstract void requestChangePassword(Callback callback);

    public User(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    public User setEmail(String email) throws InvalidEmailException, NoInternetException {
        assertConnectedToInternet();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new InvalidEmailException();
        }
        this.email = email;
        return this;
    }

    public User setPassword(String password) throws NoInternetException {
        assertConnectedToInternet();
        this.password = password;
        return this;
    }

    public User setNewPassword(String password, String confirmPassword)
            throws ShortPasswordException,
            PasswordsDontMatchException,
            NoInternetException {
        assertConnectedToInternet();
        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new ShortPasswordException();
        }
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDontMatchException();
        }
        this.password = password;
        passwordConfirmed = true;
        return this;
    }

    public void createAccount(Callback callback) throws NoInternetException {
        assertConnectedToInternet();
        this.callback = callback;
    }

    public void signIn(Callback callback) throws NoInternetException {
        assertConnectedToInternet();
        this.callback = callback;
    }

    private void assertConnectedToInternet() throws NoInternetException {
        if (!NetworkUtils.isNetworkConnected(context)) {
            throw new NoInternetException();
        }
    }

    public interface Callback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public abstract class InputException extends Exception {
        public abstract String getUserWarning();
    }

    public class InvalidEmailException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.invalid_email);
        }
    }

    public class ShortPasswordException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.password_to_short, PASSWORD_MIN_LENGTH);
        }
    }
    public class PasswordsDontMatchException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.passwords_dont_match);
        }
    }

    public class NoInternetException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.no_internet);
        }
    }

    public class FailedSignInException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.failed_sign_in);
        }
    }

    public class FailedCreateAccountException extends InputException {
        @Override
        public String getUserWarning() {
            return context.getString(R.string.failed_create_account);
        }
    }
}
