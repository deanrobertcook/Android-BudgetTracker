package org.theronin.expensetracker.pages.entries.list;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import org.theronin.expensetracker.R;

public class DeleteSelectionDialogFragment extends DialogFragment implements DialogInterface.OnClickListener{

    public static final String TAG = DeleteSelectionDialogFragment.class.getName();

    private Container container;
    private int count;

    public static DeleteSelectionDialogFragment newInstance(Container container, int count) {
        DeleteSelectionDialogFragment fragment = new DeleteSelectionDialogFragment();
        fragment.setContainer(container);
        fragment.setCount(count);
        return fragment;
    }

    private void setContainer(Container container) {
        this.container = container;
    }

    private void setCount(int count) {
        this.count = count;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getActivity().getString(R.string.delete_selection_dialog_title))
                .setMessage(String.format(getActivity().getString(R.string.delete_selection_dialog_message), count))
                .setPositiveButton(R.string.delete_selection_dialog_positive_button, this)
                .setNegativeButton(R.string.delete_selection_dialog_negative_button, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                container.onDeleteSelectionConfirmed();
                break;
        }
    }

    public interface Container {
        void onDeleteSelectionConfirmed();
    }
}
