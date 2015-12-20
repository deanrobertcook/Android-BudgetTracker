package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.theronin.expensetracker.R;

public class WelcomeFragment extends LaunchFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_welcome, container, false);
        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.next);
    }

    @Override
    public void onPositiveButtonClicked() {
        setWelcomeScreenHasBeenShown();
        setPage(LaunchPage.SIGN_IN);
    }

    private void setWelcomeScreenHasBeenShown() {
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit().putBoolean(getString(R.string.pref_welcome_screen_has_been_shown), true).commit();
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }
}
