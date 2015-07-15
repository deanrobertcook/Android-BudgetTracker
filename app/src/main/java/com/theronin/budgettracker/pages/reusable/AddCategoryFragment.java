package com.theronin.budgettracker.pages.reusable;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.theronin.budgettracker.R;
import com.theronin.budgettracker.data.BudgetDbHelper;

import static com.theronin.budgettracker.data.BudgetContract.CategoriesTable;

public class AddCategoryFragment extends Fragment {

    private BudgetDbHelper dbHelper;
    private EditText categoryEditText;
    private Button confirmNewCategoryButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment__add_category, container, false);

        categoryEditText = (EditText) rootView.findViewById(R.id.et__new_category_name);
        confirmNewCategoryButton = (Button) rootView.findViewById(R.id.btn__add_category_confirm);
        confirmNewCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConfirmNewCategoryButtonPress(view);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHelper = new BudgetDbHelper(getActivity());
    }

    @Override
    public void onPause() {
        dbHelper = null;
        super.onPause();
    }

    private void onConfirmNewCategoryButtonPress(View view) {
        String categoryName = categoryEditText.getText().toString();
        if (categoryName.length() > 0) {
            ContentValues values = new ContentValues();
            values.put(CategoriesTable.COL_CATEGORY_NAME, categoryName);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long newRowId = db.insert(CategoriesTable.TABLE_NAME, null, values);
            if (newRowId == -1) {
                Toast.makeText(getActivity(), "Make sure that the category doesn't already " +
                        "exist", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Category successfully added", Toast
                        .LENGTH_SHORT).show();
                categoryEditText.setText("");
            }

        } else {
            Toast.makeText(getActivity(), "You must specify a name for the new category", Toast
                    .LENGTH_SHORT).show();
        }
    }
}
