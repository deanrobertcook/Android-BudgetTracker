package org.theronin.expensetracker.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

public class ViewUtils {

    /**
     * Adds a padding value (in density pixels, dp) to the current existing padding
     * values of a view object
     */
    public static void addPadding(Context context, View view,
                                  int left, int top, int right, int bottom) {
        view.setPadding(
                view.getPaddingLeft() + dpToPx(context, left),
                view.getPaddingTop() + dpToPx(context, top),
                view.getPaddingRight() + dpToPx(context, right),
                view.getPaddingBottom() + dpToPx(context, bottom)
        );
    }

    public static int dpToPx(Context context, int dp) {
         return (int)(dp * (context.getResources()
                 .getDisplayMetrics().density / DisplayMetrics.DENSITY_DEFAULT));
    }
}
