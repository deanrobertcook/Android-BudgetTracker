package org.theronin.expensetracker.pages.entries.insert.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Category;

public class CategoryOptionsDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = CategoryOptionsDialogFragment.class.getName();
    private static final String ARG_CATEGORY_NAME = "CATEGORY_NAME";

    private Container container;
    private String categoryName;

    public static CategoryOptionsDialogFragment newInstance(Category category) {
        CategoryOptionsDialogFragment fragment = new CategoryOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, category.getName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        container = (Container) activity;
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        categoryName = getArguments().getString(ARG_CATEGORY_NAME);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog__category_options, null, false);

        layout.findViewById(R.id.edit).setOnClickListener(this);
        layout.findViewById(R.id.merge).setOnClickListener(this);
        layout.findViewById(R.id.delete).setOnClickListener(this);

        builder.setTitle(getActivity().getString(R.string.category_options_dialog__title, WordUtils.capitalize(categoryName)))
                .setNegativeButton(R.string.cancel, null)
                .setView(layout);

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:
                container.onEditClicked(categoryName);
                break;
            case R.id.merge:
                container.onMergeClicked(categoryName);
                break;
            case R.id.delete:
                container.onDeleteClicked(categoryName);
                break;
        }
        dismiss();
    }

    public interface Container {
        void onEditClicked(String categoryName);

        void onMergeClicked(String categoryName);

        void onDeleteClicked(String categoryName);
    }
}
