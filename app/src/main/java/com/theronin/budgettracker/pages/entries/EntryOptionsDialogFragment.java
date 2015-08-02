package com.theronin.budgettracker.pages.entries;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.theronin.budgettracker.R;

public class EntryOptionsDialogFragment extends DialogFragment {

    public static EntryOptionsDialogFragment newInstance() {
        return new EntryOptionsDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle
            savedInstanceState) {
        getDialog().setTitle("Test");
        View rootView = inflater.inflate(R.layout.fragment__entry_item_options, parent, false);
        return rootView;
    }
}
