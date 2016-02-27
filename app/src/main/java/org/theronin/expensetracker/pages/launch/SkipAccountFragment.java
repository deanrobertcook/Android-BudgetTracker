package org.theronin.expensetracker.pages.launch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.expensetracker.R;

public class SkipAccountFragment extends LaunchFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__launch_basic, container, false);
        ((TextView) view.findViewById(R.id.fragment_launch_title)).setText(R.string.launch_skip_account_title);
        ((TextView) view.findViewById(R.id.fragment_launch_body)).setText(R.string.launch_skip_account_body);
        return view;
    }

    @Override
    public String getPositiveButtonText() {
        return getActivity().getString(R.string.continue_to_registration);
    }

    @Override
    public void onPositiveButtonClicked() {
        setPage(LaunchPage.CREATE_ACCOUNT);
    }

    @Override
    public void onBackPressed() {
        setPage(LaunchPage.WELCOME);
    }

    @Override
    public void onNegativeButtonClicked() {
        setPage(LaunchPage.ENTER_APP);
    }

    @Override
    public String getNegativeButtonText() {
        return getActivity().getString(R.string.sign_up_later);
    }
}
