package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.theronin.expensetracker.R;

import timber.log.Timber;

public class SignInFragment extends LaunchFragment {

    private EditText userNameOrEmailField;
    private EditText passwordField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_sign_in, container, false);
        userNameOrEmailField = (EditText) view.findViewById(R.id.et__username_or_email);
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
        setPage(null);
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
