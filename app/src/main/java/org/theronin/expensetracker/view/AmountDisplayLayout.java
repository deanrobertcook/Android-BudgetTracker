package org.theronin.expensetracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.utils.MoneyUtils;

public class AmountDisplayLayout extends ViewGroup {

    /**
     * Code to Amount (the proportion of the total height that the amount field takes up compared
     * to the currency code). Think of it as 1:X, so, the amount field takes up (X-1)/X of the
     * total height.
     */
    private static final int CTA = 4;

    private Currency currency;

    private TextView currencySymbol;
    private TextView currencyCode;
    private TextView amountView;

    private String currentAmountDisplay;

    public AmountDisplayLayout(Context context) {
        this(context, null);
    }

    public AmountDisplayLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmountDisplayLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AmountDisplayLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.layout_amount_display, this, true);

        currencySymbol = (TextView) findViewById(R.id.currency__symbol);
        currencyCode = (TextView) findViewById(R.id.currency__code);
        amountView = (TextView) findViewById(R.id.entry_amount);
        setAmount(0);
        setCurrency(new Currency("AUD", "$", "AUD"));
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
        currencySymbol.setText(currency.symbol);
        currencyCode.setText(currency.code);
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setAmount(long amount) {
        setAmount(amount, true);
    }

    public void setAmount(long amount, boolean compact) {
        if (compact) {
            currentAmountDisplay = MoneyUtils.getDisplayCompact(getContext(), amount);
        } else {
            currentAmountDisplay = MoneyUtils.getDisplay(getContext(), amount);
        }
        amountView.setText(currentAmountDisplay);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int cSH = measuredHeight;
        int cSW = measuredWidth;
        measureView(currencySymbol, cSW, MeasureSpec.UNSPECIFIED, cSH, MeasureSpec.EXACTLY);

        int cCW = measuredWidth - currencySymbol.getMeasuredWidth();
        int cCH = measuredHeight / CTA;
        measureView(currencyCode, cCW, MeasureSpec.EXACTLY, cCH, MeasureSpec.EXACTLY);

        int aW = measuredWidth - currencySymbol.getMeasuredWidth();
        int aH = (CTA - 1) * (measuredHeight / (CTA));
        measureView(amountView, aW, MeasureSpec.EXACTLY, aH, MeasureSpec.EXACTLY);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    private void measureView(View view, int width, int widthSpec, int height, int heightSpec) {
        measureChild(view,
                MeasureSpec.makeMeasureSpec(width, widthSpec),
                MeasureSpec.makeMeasureSpec(height, heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int iLeft = getPaddingLeft();
        int iTop = getPaddingTop();

        int cSL = iLeft;
        int cST = iTop;
        int cSR = cSL + currencySymbol.getMeasuredWidth();
        int cSB = cST + currencySymbol.getMeasuredHeight();
        currencySymbol.layout(cSL, cST, cSR, cSB);

        int cCL = currencySymbol.getMeasuredWidth();
        int cCT = getMeasuredHeight() - currencyCode.getMeasuredHeight();
        int cCR = cCL + currencyCode.getMeasuredWidth();
        int cCB = cCT + currencyCode.getMeasuredHeight();
        currencyCode.layout(cCL, cCT, cCR, cCB);

        int aL = currencySymbol.getMeasuredWidth();
        int aT = iTop;
        int aR = aL + amountView.getMeasuredWidth();
        int aB = aT + amountView.getMeasuredHeight();
        amountView.layout(aL, aT, aR, aB);
    }

    public void setOnEditorActionListener(TextView.OnEditorActionListener actionListener) {
        amountView.setOnEditorActionListener(actionListener);
    }
}
