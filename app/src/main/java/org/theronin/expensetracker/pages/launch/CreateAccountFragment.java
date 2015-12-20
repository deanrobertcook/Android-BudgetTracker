package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.theronin.expensetracker.R;

import timber.log.Timber;

public class CreateAccountFragment extends LaunchFragment {

    private EditText userNameField;
    private EditText emailField;
    private EditText passwordField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_create_account, container, false);
        userNameField = (EditText) view.findViewById(R.id.et__username);
        emailField = (EditText) view.findViewById(R.id.et__email);
        passwordField = (EditText) view.findViewById(R.id.et__password);
        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.sign_up);
    }

    @Override
    public String getTertiaryButtonText() {
        return getActivity().getString(R.string.existing_account);
    }

    @Override
    public void onPositiveButtonClicked() {
        Timber.d("username: " + userNameField.getText().toString());
        Timber.d("email: " + emailField.getText().toString());
        Timber.d("password: " + passwordField.getText().toString());
        //TODO handle input credentials properly
        ParseUser user = new ParseUser();
        user.setUsername(userNameField.getText().toString());
        user.setEmail(emailField.getText().toString());
        user.setPassword(passwordField.getText().toString());

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setPage(null);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTertiaryButtonClicked() {
        setPage(LaunchPage.SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        setPage(LaunchPage.SIGN_IN);
    }
}
