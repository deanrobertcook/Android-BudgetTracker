package org.theronin.budgettracker.utils;

import android.content.Context;
import android.view.View;

public class ViewUtils {

    /**
     * Adds a padding value (in density pixels, dp) to the current existing padding
     * values of a view object
     */
    public static void addPadding(Context context, View view,
                                  int left, int top, int right, int bottom) {
        view.setPadding(
                view.getPaddingLeft() + getPixels(context, left),
                view.getPaddingTop() + getPixels(context, top),
                view.getPaddingRight() + getPixels(context, right),
                view.getPaddingBottom() + getPixels(context, bottom)
        );
    }

    public static int getPixels(Context context, int dp) {
         return (int)(dp * (context.getResources().getDisplayMetrics().densityDpi / 160));
    }
}
