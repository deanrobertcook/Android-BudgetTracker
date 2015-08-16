package com.theronin.budgettracker.pages.categories;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetContract;

public class AddCategoryFragment extends Fragment implements
        TextView.OnEditorActionListener {

    private EditText categoryEditText;
    private Button confirmNewCategoryButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_category, container, false);

        categoryEditText = (EditText) rootView.findViewById(R.id.et__new_category_name);
        categoryEditText.setOnEditorActionListener(this);

        confirmNewCategoryButton = (Button) rootView.findViewById(R.id.btn__add_category_confirm);
        confirmNewCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmNewCategoryButtonPress();
            }
        });

        return rootView;
    }

    private void onConfirmNewCategoryButtonPress() {
        String categoryName = categoryEditText.getText().toString();
        categoryName = categoryName.toLowerCase().trim();
        addCategory(categoryName);

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    private void clearCategoryEditText() {
        categoryEditText.setText("");
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;

        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onConfirmNewCategoryButtonPress();
            handled = true;
        }
        return handled;
    }

    public void addCategory(String categoryName) {
        if (categoryName == null || categoryName.equals("")) {
            Toast.makeText(getActivity(), "You must specify a name for the new category", Toast
                    .LENGTH_SHORT).show();
        }

        ContentValues values = new ContentValues();
        values.put(BudgetContract.CategoriesTable.COL_CATEGORY_NAME, categoryName);

        Uri categoryUri = getActivity().getContentResolver().insert(
                BudgetContract.CategoriesTable.CONTENT_URI,
                values
        );

        long id = Long.parseLong(categoryUri.getLastPathSegment());

        if (id == -1) {
            Toast.makeText(getActivity(), "Make sure that the category doesn't already exist",
                    Toast.LENGTH_SHORT).show();
        } else {
            clearCategoryEditText();

            Toast.makeText(getActivity(), "Category added succesfully",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
