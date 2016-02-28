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

public class SignInFragment extends LaunchFragment {

    private EditText emailField;
    private EditText passwordField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_sign_in, container, false);
        emailField = (EditText) view.findViewById(R.id.et__email);
        passwordField = (EditText) view.findViewById(R.id.et__password);
        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.sign_in_caps);
    }

    @Override
    public String getTertiaryButtonText() {
        return getActivity().getString(R.string.create_account_caps);
    }

    @Override
    public void onPositiveButtonClicked() {
        try {
            new ParseUserWrapper(getActivity())
                    .setEmail(emailField.getText().toString())
                    .setPassword(passwordField.getText().toString())
                    .signIn(new User.Callback() {
                        @Override
                        public void onSuccess() {
                            setPage(LaunchPage.ENTER_APP);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getActivity(), getActivity().getString(R.string.failed_sign_in), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (User.InputException e) {
            Toast.makeText(getActivity(), e.getUserWarning(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTertiaryButtonClicked() {
        setPage(LaunchPage.CREATE_ACCOUNT);
    }

    @Override
    public void onBackPressed() {
        setPage(LaunchPage.CREATE_ACCOUNT);
    }
}
