package org.theronin.expensetracker.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import org.theronin.expensetracker.utils.MoneyUtils;

public class MoneyEditText extends VerticalResizeEditText {

    private String currentAmountDisplay;
    private CustomTextWatcher textWatcher;

    public MoneyEditText(Context context) {
        this(context, null);
    }

    public MoneyEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public MoneyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        textWatcher = new CustomTextWatcher();
        addTextChangedListener(textWatcher);
    }

    private class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence input, int start, int before, int count) {
            if (!input.toString().equals(currentAmountDisplay) && input.length() != 0) {
                String cleanString = input.toString().replaceAll("[,.]", "");
                long cents = Long.parseLong(cleanString);
                setAmount(cents);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    public void setAmount(long amount) {
        String amountDisplay = MoneyUtils.getDisplay(getContext(), amount);
        removeTextChangedListener(textWatcher);

        currentAmountDisplay = amountDisplay;

        setText(currentAmountDisplay);
        setSelection(currentAmountDisplay.length());

        addTextChangedListener(textWatcher);
    }

    public long getAmount() {
        return MoneyUtils.getCents(currentAmountDisplay);
    }
}
