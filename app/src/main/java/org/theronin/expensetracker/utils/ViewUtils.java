package org.theronin.expensetracker.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

import timber.log.Timber;

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

    public static void layoutDebug(String name, int l, int t, int r, int b) {
        Timber.d("%s:, l: %d, t: %d, r: %d, b: %d", name, l, t, r, b);
    }

    public static void measureDebug(String name, int width, int height) {
        Timber.v("%s: width: %d, height: %d", name, width, height);
    }
}
