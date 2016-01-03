package org.theronin.expensetracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Currency;

public class AmountEditLayout extends ViewGroup {

    private TextView currencySymbol;
    private TextView currencyCode;

    private MoneyEditText amountView;

    public AmountEditLayout(Context context) {
        this(context, null);
    }

    public AmountEditLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmountEditLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmountEditLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.layout_amount_input, this, true);

        currencySymbol = (TextView) findViewById(R.id.currency__symbol);
        currencyCode = (TextView) findViewById(R.id.currency__code);
        amountView = (MoneyEditText) findViewById(R.id.entry_amount);
    }

    public void setCurrency(Currency currency) {
        currencySymbol.setText(currency.symbol);
        currencyCode.setText(currency.code);
        invalidate();
    }

    public void setAmount(long amount) {
        amountView.setAmount(amount);
        invalidate();
    }

    public long getAmount() {
        return amountView.getAmount();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();

        int topPos = getPaddingTop();
        int bottomPos = bottom - top - getPaddingBottom();

        currencySymbol.layout(leftPos, topPos, currencySymbol.getWidth(), currencySymbol.getHeight());

        int codeLeft = leftPos + currencySymbol.getRight();
        int codeTop = bottomPos - currencyCode.getHeight();
        currencyCode.layout(codeLeft, codeTop, currencyCode.getWidth(), currencyCode.getHeight());

        int editTextLeft = leftPos + currencySymbol.getRight();
        int editTextWidth = rightPos - currencySymbol.getRight();
        int editTextHeight = codeTop - topPos;
        amountView.layout(editTextLeft, topPos, editTextWidth, editTextHeight);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener actionListener) {
        amountView.setOnEditorActionListener(actionListener);
    }
}
