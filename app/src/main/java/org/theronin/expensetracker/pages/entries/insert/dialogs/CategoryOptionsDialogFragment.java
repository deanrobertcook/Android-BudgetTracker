package org.theronin.expensetracker.pages.entries.insert.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Category;

public class CategoryOptionsDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = CategoryOptionsDialogFragment.class.getName();
    private static final String ARG_CATEGORY = "CATEGORY";

    private Container container;
    private Category category;

    public static CategoryOptionsDialogFragment newInstance(Category category) {
        CategoryOptionsDialogFragment fragment = new CategoryOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
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
        category = (Category) getArguments().getSerializable(ARG_CATEGORY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog__category_options, null, false);

        layout.findViewById(R.id.edit).setOnClickListener(this);
        layout.findViewById(R.id.merge).setOnClickListener(this);
        layout.findViewById(R.id.delete).setOnClickListener(this);

        builder.setTitle(getActivity().getString(R.string.category_options_dialog__title, category.getDisplayName()))
                .setView(layout);

        return builder.create();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit:
                container.onEditClicked(category);
                break;
            case R.id.merge:
                container.onMergeClicked(category);
                break;
            case R.id.delete:
                container.onDeleteClicked(category);
                break;
        }
        dismiss();
    }

    public interface Container {
        void onEditClicked(Category category);

        void onMergeClicked(Category category);

        void onDeleteClicked(Category category);
    }
}
