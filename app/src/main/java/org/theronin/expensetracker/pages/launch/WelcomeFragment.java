package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.utils.Prefs;

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
        Prefs.setWelcomeScreenShown(getActivity());
        setPage(LaunchPage.SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }
}
