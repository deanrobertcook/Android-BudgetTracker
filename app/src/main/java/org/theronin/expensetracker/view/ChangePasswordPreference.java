package org.theronin.expensetracker.view;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.user.User;
import org.theronin.expensetracker.model.user.UserManager;

public class ChangePasswordPreference extends DialogPreference {

    public ChangePasswordPreference(Context context) {
        this(context, null);
    }

    public ChangePasswordPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public ChangePasswordPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ChangePasswordPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setDialogTitle(R.string.dialog_change_password_title);
        setDialogMessage(R.string.dialog_change_password_body);
        setPositiveButtonText(R.string.send_caps);
        setNegativeButtonText("");
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            UserManager.getUser(getContext()).requestChangePassword(new User.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Email successfully sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Something went wrong sending your password change email. Please try again later",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
