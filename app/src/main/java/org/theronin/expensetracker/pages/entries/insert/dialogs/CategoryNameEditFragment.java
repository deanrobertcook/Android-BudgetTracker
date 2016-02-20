package org.theronin.expensetracker.pages.entries.insert.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.theronin.expensetracker.R;

public class CategoryNameEditFragment extends DialogFragment {

    public static final String TAG = CategoryNameEditFragment.class.getName();
    private static final String ARG_CATEGORY_NAME = "CATEGORY_NAME";

    private Container container;

    public static CategoryNameEditFragment newInstance(String categoryName) {
        CategoryNameEditFragment fragment = new CategoryNameEditFragment();

        if (categoryName != null) {
            Bundle args = new Bundle();
            args.putString(ARG_CATEGORY_NAME, categoryName);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        container = (Container) activity;
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String categoryName = getArguments() == null ? null : getArguments().getString(ARG_CATEGORY_NAME);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog__category_name, null, false);
        final EditText editText = (EditText) layout.findViewById(R.id.edit_category);

        if (categoryName != null) {
            editText.setText(categoryName);
            editText.setSelection(categoryName.length());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(categoryName == null ? R.string.category_name_dialog__title_new : R.string.category_name_dialog__title_edit)
                .setPositiveButton(categoryName == null ? R.string.category_name_dialog__positive_button_new : R.string.category_name_dialog__positive_button_edit, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                container.onPositiveButtonClicked(categoryName, editText.getText().toString());
                            }
                        })
                .setView(layout);

        return builder.create();
    }

    public interface Container {
        void onPositiveButtonClicked(String oldCategoryName, String newCategoryName);
    }
}
