package com.theronin.budgettracker.pages.categories;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.theronin.budgettracker.R;

public class AddCategoryFragment extends Fragment implements TextView.OnEditorActionListener {

    private EditText categoryEditText;
    private Button confirmNewCategoryButton;
    private Container container;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        container = (Container) activity;
    }

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void onConfirmNewCategoryButtonPress() {
        String categoryName = categoryEditText.getText().toString();
        categoryName = categoryName.toLowerCase().trim();
        container.onCategoryAdded(categoryName);

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public void clearCategoryEditText() {
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

    public interface Container {
        public void onCategoryAdded(String categoryName);
    }
}
