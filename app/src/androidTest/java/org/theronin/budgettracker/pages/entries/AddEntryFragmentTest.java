package org.theronin.budgettracker.pages.entries;

import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.theronin.budgettracker.DatabaseDevUtils;
import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetDbHelper;
import org.theronin.budgettracker.model.Entry;
import org.theronin.budgettracker.pages.ActivityTestCase;
import org.theronin.budgettracker.utils.DateUtils;
import org.theronin.budgettracker.utils.MoneyUtils;

import org.junit.runner.RunWith;
import org.theronin.budgettracker.EspressoUtils;

@RunWith(AndroidJUnit4.class)
public class AddEntryFragmentTest extends ActivityTestCase<AddEntryFragmentTestActivity> {

    public AddEntryFragmentTest() {
        super(AddEntryFragmentTestActivity.class);
    }

//    @Test
// TODO figure out how to get these UI tests to work for an app component
    public void insertEntry() {
        BudgetDbHelper dbHelper = new BudgetDbHelper(activity);
        String[] testCategories = new String[] {"cashews"};
        int numDummyEntries = 0;
        int maxAmount = 0;

        DatabaseDevUtils.fillDatabaseWithDummyData(dbHelper, testCategories, numDummyEntries, maxAmount);

        String expectedCategory = testCategories[0];
        String expectedDate = DateUtils.getStorageFormattedCurrentDate();
        long expectedAmount = 1050; //$10.50
        Entry entryToInsert = new Entry(expectedCategory, expectedDate, expectedAmount);

        View rootView = activity.addEntryFragment.getView();
        EditText amountEditText = (EditText) rootView.findViewById(R.id.et__entry_amount);
        Spinner categorySpinner = (Spinner) rootView.findViewById(R.id.spn__entry_category);
        TextView dateEditText = (TextView) rootView.findViewById(R.id.tv__entry_date);

        amountEditText.setText(MoneyUtils.convertToDollars(entryToInsert.amount));
        categorySpinner.setSelection(((ArrayAdapter) categorySpinner.getAdapter()).getPosition
                (entryToInsert.category));
        dateEditText.setText(entryToInsert.utcDate);

        EspressoUtils.click(R.id.btn__add_entry_confirm);

    }
}
