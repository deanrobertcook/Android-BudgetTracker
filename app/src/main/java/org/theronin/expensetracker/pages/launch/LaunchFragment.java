package org.theronin.expensetracker.pages.launch;

import android.app.Fragment;

public abstract class LaunchFragment extends Fragment {

    protected void setPage(LaunchPage page) {
        if (getActivity() != null) {
            ((LaunchActivity) getActivity()).setPage(page);
        }
    }

    public abstract String getPositiveButtonText();

    public String getNegativeButtonText() {
        return null;
    }

    public String getTertiaryButtonText() {
        return null;
    }

    public abstract void onPositiveButtonClicked();

    public void onNegativeButtonClicked() {
        //do nothing
    }

    public void onTertiaryButtonClicked() {
        //do nothing
    }

    public abstract void onBackPressed();
}
