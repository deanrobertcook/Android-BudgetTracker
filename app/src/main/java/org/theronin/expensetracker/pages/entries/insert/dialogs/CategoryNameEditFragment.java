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
import org.theronin.expensetracker.model.Category;

public class CategoryNameEditFragment extends DialogFragment {

    public static final String TAG = CategoryNameEditFragment.class.getName();
    private static final String ARG_CATEGORY = "CATEGORY";

    private Container container;

    public static CategoryNameEditFragment newInstance(Category category) {
        CategoryNameEditFragment fragment = new CategoryNameEditFragment();

        if (category != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_CATEGORY, category);
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
        final Category category = getArguments() == null ? null : (Category) getArguments().getSerializable(ARG_CATEGORY);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog__category_name, null, false);
        final EditText editText = (EditText) layout.findViewById(R.id.edit_category);

        if (category != null) {
            editText.setText(category.getDisplayName());
            editText.setSelection(category.getDisplayName().length());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(category == null ? R.string.category_name_dialog__title_new : R.string.category_name_dialog__title_edit)
                .setPositiveButton(category == null ? R.string.category_name_dialog__positive_button_new : R.string.category_name_dialog__positive_button_edit, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                container.onPositiveButtonClicked(category, editText.getText().toString());
                            }
                        })
                .setView(layout);

        return builder.create();
    }

    public interface Container {
        void onPositiveButtonClicked(Category category, String newCategoryName);
    }
}
