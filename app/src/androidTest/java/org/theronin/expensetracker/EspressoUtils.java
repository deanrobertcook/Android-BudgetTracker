package org.theronin.expensetracker;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;

import org.hamcrest.CoreMatchers;

public class EspressoUtils {
    public static void assertDisplayed(int elementId) {
        Espresso.onView(ViewMatchers.withId(elementId))
                .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()));
    }

    public static void assertNotDisplayed(int elementId) {
        Espresso.onView(ViewMatchers.withId(elementId))
                .check(ViewAssertions.matches(CoreMatchers.not(ViewMatchers
                        .isCompletelyDisplayed())));
    }

    public static void perform(int elementId, ViewAction action) {
        Espresso.onView(ViewMatchers.withId(elementId)).perform(action);
    }

    public static void click(int elementId) {
        Espresso.onView(ViewMatchers.withId(elementId)).perform(ViewActions.click());
    }
}
