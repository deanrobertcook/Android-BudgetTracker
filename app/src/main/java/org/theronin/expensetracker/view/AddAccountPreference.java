package org.theronin.expensetracker.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.user.ParseUserWrapper;
import org.theronin.expensetracker.model.user.User;

public class AddAccountPreference extends DialogPreference {

    private Listener listener;

    public interface Listener {
        void onAccountAdded();
    }

    private Dialog createAccountDialog;
    private Dialog signupDialog;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;

    public AddAccountPreference(Context context) {
        super(context);
    }

    public AddAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AddAccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AddAccountPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void showDialog(Bundle state) {
        showCreateAccountDialog();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (dialog == createAccountDialog) {
                    createUser();
                } else if (dialog == signupDialog) {
                    signInUser();
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                if (dialog == createAccountDialog) {
                    showSignInDialog();
                } else if (dialog == signupDialog) {
                    showCreateAccountDialog();
                }
                break;
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void createUser() {
        assertListenerAvailable();
        new ParseUserWrapper()
                .setEmail(emailEditText.getText().toString())
                .setNewPassword(
                        passwordEditText.getText().toString(),
                        confirmEditText.getText().toString()
                )
                .createAccount(new User.Callback() {
                    @Override
                    public void onSuccess() {
                        listener.onAccountAdded();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Something went wrong signing up", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signInUser() {
        assertListenerAvailable();
        new ParseUserWrapper()
                .setEmail(emailEditText.getText().toString())
                .setPassword(passwordEditText.getText().toString())
                .signIn(new User.Callback() {
                    @Override
                    public void onSuccess() {
                        listener.onAccountAdded();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Something went wrong signing in", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void assertListenerAvailable() {
        if (listener == null) {
            throw new IllegalStateException("There needs to be a listener to change the database on " +
                                            "successfully adding an account");
        }
    }

    private void showCreateAccountDialog() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account_preference, null);
        findFields(view);

        createAccountDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.create_account)
                .setView(view)
                .setNeutralButton(R.string.existing_account, this)
                .setPositiveButton(R.string.sign_up_caps, this)
                .create();
        createAccountDialog.show();
    }

    private void showSignInDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sign_up_preference, null);
        findFields(view);
        signupDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.sign_in)
                .setView(view)
                .setNeutralButton(R.string.create_account_caps, this)
                .setPositiveButton(R.string.sign_in_caps, this)
                .create();
        signupDialog.show();
    }

    private void findFields(View view) {
        emailEditText = (EditText) view.findViewById(R.id.et__email);
        passwordEditText = (EditText) view.findViewById(R.id.et__password);
        confirmEditText = (EditText) view.findViewById(R.id.et__confirm_password);
    }
}
