package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.user.ParseUserWrapper;
import org.theronin.expensetracker.model.user.User;

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
        new ParseUserWrapper()
                .setEmail(emailField.getText().toString())
                .setNewPassword(
                        passwordField.getText().toString(),
                        confirmPasswordField.getText().toString()
                )
                .createAccount(new User.Callback() {
                    @Override
                    public void onSuccess() {
                        setPage(LaunchPage.ENTER_APP);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
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
