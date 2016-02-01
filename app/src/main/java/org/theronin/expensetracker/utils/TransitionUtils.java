package org.theronin.expensetracker.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.os.Bundle;

import org.theronin.expensetracker.R;

public class TransitionUtils {
    public static Bundle getLeftTransitionAnimation(Context context) {
        return ActivityOptions.makeCustomAnimation(context, R.anim.right_to_left_in, R.anim.right_to_left_out).toBundle();
    }
}
