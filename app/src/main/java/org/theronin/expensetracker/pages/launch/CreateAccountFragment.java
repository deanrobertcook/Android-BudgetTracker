package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.theronin.expensetracker.R;

import timber.log.Timber;

public class CreateAccountFragment extends LaunchFragment {

    private EditText emailField;
    private EditText passwordField;
    private EditText confirmPasswordField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_create_account, container, false);
        emailField = (EditText) view.findViewById(R.id.et__email);
        passwordField = (EditText) view.findViewById(R.id.et__password);
        confirmPasswordField = (EditText) view.findViewById(R.id.et__confirm_password);
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
        Timber.d("email: " + emailField.getText().toString());
        Timber.d("password: " + passwordField.getText().toString());

        String password = passwordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
        }

        ParseUser user = new ParseUser();
        user.setEmail(emailField.getText().toString());
        user.setUsername(emailField.getText().toString());
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setPage(LaunchPage.ENTER_APP);
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        setPage(LaunchPage.SKIP_ACCOUNT);
    }
}
