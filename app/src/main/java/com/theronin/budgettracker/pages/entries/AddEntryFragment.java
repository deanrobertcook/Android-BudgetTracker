package com.theronin.budgettracker.pages.entries;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.model.Category;
import com.theronin.budgettracker.model.CategoryStore;
import com.theronin.budgettracker.model.Entry;
import com.theronin.budgettracker.model.EntryStore;
import com.theronin.budgettracker.pages.reusable.DatePickerFragment;
import com.theronin.budgettracker.utils.DateUtils;
import com.theronin.budgettracker.utils.MoneyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

public class AddEntryFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        DatePickerFragment.Container,
        CategoryStore.Observer,
        TextView.OnEditorActionListener {

    private static final String TAG = AddEntryFragment.class.getName();

    private Container container;

    private TextView currencySymbolTextView;
    private Button confirmEntryButton;

    private Spinner categorySpinner;
    private ArrayAdapter<String> categorySpinnerAdapter;

    private EditText dateEditText;
    private EditText amountEditText;
    private String lastSelectedCategory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_entry, container, false);

        currencySymbolTextView = (TextView) rootView.findViewById(R.id.tv__currency_symbol);
        currencySymbolTextView.setText(MoneyUtils.getCurrencySymbol());

        confirmEntryButton = (Button) rootView.findViewById(R.id.btn__add_entry_confirm);
        confirmEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passInputToStore();
            }
        });

        amountEditText = (EditText) rootView.findViewById(R.id.et__entry_amount);
        amountEditText.setOnEditorActionListener(this);

        categorySpinner = (Spinner) rootView.findViewById(R.id.spn__entry_category);
        categorySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categorySpinnerAdapter);
        categorySpinner.setOnItemSelectedListener(this);

        dateEditText = (EditText) rootView.findViewById(R.id.et__entry_date);
        setDateText(System.currentTimeMillis());
        dateEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                dateEditTextClicked(view);
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.container = (Container) activity;
    }

    private void dateEditTextClicked(View view) {
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setContainer(this);
        datePickerFragment.show(getFragmentManager(), TAG);
    }

    private void passInputToStore() {
        String amountVal = amountEditText.getText().toString();
        if (amountVal.length() == 0) {
            return;
        }

        long amount = MoneyUtils.convertToCents(amountVal);

        lastSelectedCategory = categorySpinner.getSelectedItem().toString();
        String dateEnteredVal = dateEditText.getText().toString();

        long id = container.getEntryStore().addEntry(new Entry(lastSelectedCategory, dateEnteredVal, amount));

        if (id == -1) {
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "All G", Toast.LENGTH_SHORT).show();
            amountEditText.setText("");

            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void setDateText(long utcDate) {
        String date = DateUtils.formatDate(utcDate);
        if (dateEditText != null) {
            dateEditText.setText(date);
        }
    }

    @Override
    public void onDestroyView() {
        this.currencySymbolTextView = null;
        this.confirmEntryButton = null;
        this.categorySpinner = null;

        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (view != null) {
            Timber.d(((TextView) view).getText() + " selected");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }


    @Override
    public void onDateSelected(long utcDate) {
        setDateText(utcDate);
    }

    @Override
    public void onCategoriesLoaded(List<Category> categories) {

        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                return (int) (rhs.frequency - lhs.frequency);
            }
        });

        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.name);
        }

        categorySpinnerAdapter.clear();
        categorySpinnerAdapter.addAll(categoryNames);
        categorySpinnerAdapter.notifyDataSetChanged();

        if (lastSelectedCategory != null) {
            int lastSelectedCategoryNewPosition = categorySpinnerAdapter.getPosition(lastSelectedCategory);
            categorySpinner.setSelection(lastSelectedCategoryNewPosition);
            //If you don't set this back to null, then there are cases where the lastSelectedCategory
            //gets out of date.
            lastSelectedCategory = null;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            passInputToStore();
            handled = true;
        }
        return handled;
    }

    public interface Container {
        EntryStore getEntryStore();
    }
}
