package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.theronin.expensetracker.R;

import timber.log.Timber;

public class SignInFragment extends LaunchFragment {

    private EditText userNameOrEmailField;
    private EditText passwordField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_sign_in, container, false);
        userNameOrEmailField = (EditText) view.findViewById(R.id.et__email);
        passwordField = (EditText) view.findViewById(R.id.et__password);
        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.sign_in);
    }

    @Override
    public String getTertiaryButtonText() {
        return getActivity().getString(R.string.create_account);
    }

    @Override
    public void onPositiveButtonClicked() {
        Timber.d("userNameOrEmail: " + userNameOrEmailField.getText().toString());
        Timber.d("password: " + passwordField.getText().toString());

        ParseUser.logInInBackground(
                userNameOrEmailField.getText().toString(),
                passwordField.getText().toString(),
                new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null) {
                            setPage(null);
                        } else {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Couldn't log in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onTertiaryButtonClicked() {
        setPage(LaunchPage.CREATE_ACCOUNT);
    }

    @Override
    public void onBackPressed() {
        setPage(LaunchPage.WELCOME);
    }
}
