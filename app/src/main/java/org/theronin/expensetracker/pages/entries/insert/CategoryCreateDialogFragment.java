package org.theronin.expensetracker.pages.entries.insert;

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

public class CategoryCreateDialogFragment extends DialogFragment {

    public static final String TAG = CategoryCreateDialogFragment.class.getName();

    private Container container;

    @Override
    public void onAttach(Activity activity) {
        container = (Container) activity;
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View layout = inflater.inflate(R.layout.dialog__add_category, null, false);

        builder.setTitle(getActivity().getString(R.string.create_category_dialog__title))
                .setPositiveButton(R.string.create_category_dialog__positive_button, new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText editText = (EditText) layout.findViewById(R.id.et__create_category_dialog);
                                container.onCategoryCreated(editText.getText().toString());
                            }
                        })
                .setView(layout);

        return builder.create();
    }

    public interface Container {
        void onCategoryCreated(String categoryName);
    }
}
