package org.theronin.expensetracker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class VerticalResizeTextView extends TextView {

    public VerticalResizeTextView(Context context) {
        this(context, null);
    }

    public VerticalResizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Override the set text size and swallow the value
     */
    @Override
    public void setTextSize(float size) {

    }

    /**
     * Override the set text size and swallow the value
     */
    @Override
    public void setTextSize(int unit, float size) {
    }

    /**
     * Override the set line spacing and swallow the value
     */
    @Override
    public void setLineSpacing(float add, float mult) {
    }

    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();
        resizeText(heightLimit);
        super.onLayout(changed, left, top, right, bottom);
    }

    /**
     * Resize the text size to fit within the specified height
     * @param height
     */
    public void resizeText(int height) {
        CharSequence text = getText();
        if (text == null || text.length() == 0 || height <= 0) {
            return;
        }
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, height);
    }
}