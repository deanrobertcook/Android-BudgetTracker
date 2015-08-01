package com.theronin.budgettracker.pages.entries;

import android.app.Activity;
import android.os.Bundle;

import com.theronin.budgettracker.test.R;


public class AddEntryFragmentTestActivity extends Activity {

    protected AddEntryFragment addEntryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__add_entry_fragment);

        addEntryFragment = (AddEntryFragment) getFragmentManager().findFragmentById(R.id.fragment__add_entry);
    }
}
