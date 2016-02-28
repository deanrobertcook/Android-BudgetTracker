package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

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

        AwesomeValidation validation = new AwesomeValidation(ValidationStyle.BASIC);
        validation.addValidation(getActivity(), emailField.getId(), Patterns.EMAIL_ADDRESS, R.string.invalid_email);
        validation.addValidation(getActivity(), passwordField.getId(), ".^{8}$", R.string.password_to_short);
        validation.addValidation(getActivity(), confirmPasswordField.getId(), ".^{8}$", R.string.invalid_email);

        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.sign_up_caps);
    }

    @Override
    public String getTertiaryButtonText() {
        return getActivity().getString(R.string.existing_account);
    }

    @Override
    public void onPositiveButtonClicked() {
        try {
            new ParseUserWrapper(getActivity())
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
                            //TODO tidy up these Parse failures a bit
                            if (e.getMessage().contains("has already been taken")) {
                                Toast.makeText(getActivity(), getActivity().getString(R.string.credentials_taken), Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(getActivity(), getActivity().getString(R.string.failed_create_account), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (User.InputException e) {
            Toast.makeText(getActivity(), e.getUserWarning(), Toast.LENGTH_SHORT).show();
        }

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
