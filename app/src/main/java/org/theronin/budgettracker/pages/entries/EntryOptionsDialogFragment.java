package org.theronin.budgettracker.pages.entries;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.budgettracker.R;


public class EntryOptionsDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView deleteTextView;
    private Container container;

    public static EntryOptionsDialogFragment newInstance(Container container) {
        EntryOptionsDialogFragment fragment = new EntryOptionsDialogFragment();
        fragment.setContainer(container);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.OptionsDialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle
            savedInstanceState) {
        getDialog().setTitle("Test");
        View rootView = inflater.inflate(R.layout.fragment__entry_item_options, parent, false);
        deleteTextView = (TextView) rootView.findViewById(R.id.tv__delete);
        deleteTextView.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv__delete) {
            container.onDeleteClicked();
        }
        this.dismiss();
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    interface Container {
        void onDeleteClicked();
    }
}
