package com.theronin.budgettracker;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

public class ActivityTestCase<T extends Activity> {

        /**
         * This rule drives the creation of the test activity.
         */
        @Rule
        public final ActivityTestRule<T> activityRule;

        /**
         * This rule is what sets the animation scale to zero to prevent test flakiness. In order
         * for it to function, the test APK needs to ask for the {@link android.Manifest
         * .permission#SET_ANIMATION_SCALE} permission and this permission also needs to be granted
         * via adb.
         */
        @Rule
        public final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

        /**
         * The Instrumentation instance which is used to drive the tests. With this object, we can
         * run parts of our test that modify views on the main thread using {@link
         * Instrumentation#runOnMainSync(Runnable)} and then block until that action is finished
         * using {@link Instrumentation#waitForIdleSync()}
         */
        protected final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        /**
         * An activity of the type given to the constructor for ZetaTestCase. This would be some form
         * of minimal activity whose only purpose is to host a view or fragment and provide a context
         * in which we can interact with that element. The activity is destroyed and recreated between
         * each test.
         */
        protected T activity;

        /**
         * Constructor required to get type of the test Activity for the ActivityTestRule
         *
         * @param testActivityType the type of the test Activity
         */
        protected ActivityTestCase(Class<T> testActivityType) {
            activityRule = new ActivityTestRule<T>(testActivityType);
        }

        /**
         * Initializes the test activity from the ActivityTestRule making it available to all subclass
         * tests.
         */
        @Before
        public void initializeActivity() {
            activity = activityRule.getActivity();
        }
    }


}
