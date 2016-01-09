package org.theronin.expensetracker.pages.entries.insert;

import android.view.View;
import android.widget.TextView;

import org.apache.commons.lang.WordUtils;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.view.MoneyEditText;

public class InsertRowViewHolder implements View.OnClickListener {
    private int rowIndex = -1;
    private RowClickListener listener;

    public final View rowView;
    public final View clearButton;
    public final MoneyEditText moneyEditText;

    private final TextView categorySelectorTextView;
    private Category category;

    public InsertRowViewHolder(View rowView, RowClickListener listener, int rowIndex) {
        this.rowView = rowView;
        this.listener = listener;
        this.rowIndex = rowIndex;

        clearButton = rowView.findViewById(R.id.clear_row);
        clearButton.setOnClickListener(this);
        moneyEditText = (MoneyEditText) rowView.findViewById(R.id.amount_edit_layout);
        categorySelectorTextView = (TextView) rowView.findViewById(R.id.tv__add_entry_category);
        //TODO move to string resource
        categorySelectorTextView.setText("----");
        categorySelectorTextView.setOnClickListener(this);
    }

    public void resetRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setCategory(Category category) {
        this.category = category;
        categorySelectorTextView.setText(WordUtils.capitalize(category.name));
    }

    public Category getCategory() {
        return category;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_row:
                listener.onRowClearButtonClicked(rowIndex);
                break;
            case R.id.tv__add_entry_category:
                listener.onRowSelectCategoryFieldClicked(rowIndex);
                break;
        }
    }

    public interface RowClickListener {
        void onRowClearButtonClicked(int rowIndex);
        void onRowSelectCategoryFieldClicked(int rowIndex);
    }
}
