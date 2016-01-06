package org.theronin.expensetracker.pages.launch;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.parse.ParseUser;

import org.theronin.expensetracker.CustomApplication;
import org.theronin.expensetracker.R;
import org.theronin.expensetracker.pages.main.MainActivity;

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    private LaunchFragment currentFragment;

    private Button positiveButton;
    private Button negativeButton;
    private Button tertiaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity__launch);
        positiveButton = (Button) findViewById(R.id.button_positive);
        negativeButton = (Button) findViewById(R.id.button_negative);
        tertiaryButton = (Button) findViewById(R.id.button_tertiary);
        setPage(getFirstPageToDisplay());
    }

    /**
     * Set the current launch page. Setting a null page will trigger the main portion of the app.
     * @param page the page to display, or null to enter the app.
     */
    public void setPage(LaunchPage page) {
        if (startApplication(page)) {
            finish();
            return;
        }
        currentFragment = page.fragment;
        getFragmentManager().beginTransaction()
                .replace(R.id.welcome_content, currentFragment)
                .commit();
    }

    private boolean startApplication(LaunchPage page) {
        if (page == LaunchPage.ENTER_APP) {
            if (ParseUser.getCurrentUser() == null) {
                throw new IllegalStateException("The app can only be entered when there is a signed in user");
            }
            //Create or Set the database for the user:
            ((CustomApplication) getApplication()).setDatabase();

            Intent startAppIntent = new Intent(this, MainActivity.class);
            startActivity(startAppIntent);
            return true;
        }
        return false;
    }

    private LaunchPage getFirstPageToDisplay() {
        if (!hasWelcomeScreenBeenShown()) {
            return LaunchPage.WELCOME;
        }
        if (ParseUser.getCurrentUser() == null) {
            return LaunchPage.SIGN_IN;
        }
        return LaunchPage.ENTER_APP;
        //return the welcome page to manually test signing in with different users
//        return LaunchPage.WELCOME;
    }

    private boolean hasWelcomeScreenBeenShown() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(this.getString(R.string.pref_welcome_screen_has_been_shown), false);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        wireUpButtons();
    }

    private void wireUpButtons() {
        //TODO hunt for NPEs around here
        //TODO https://github.com/deanrobertcook/Android-ExpenseTracker/issues/33
        positiveButton.setText(currentFragment.getPositiveButtonText());

        String negativeButtonText = currentFragment.getNegativeButtonText();
        if (negativeButtonText == null) {
            negativeButton.setVisibility(View.GONE);
        } else {
            negativeButton.setVisibility(View.VISIBLE);
            negativeButton.setText(negativeButtonText);
        }

        String tertiaryButtonText = currentFragment.getTertiaryButtonText();
        if (tertiaryButtonText == null) {
            tertiaryButton.setVisibility(View.GONE);
        } else {
            tertiaryButton.setVisibility(View.VISIBLE);
            tertiaryButton.setText(tertiaryButtonText);
        }

        positiveButton.setOnClickListener(this);
        negativeButton.setOnClickListener(this);
        tertiaryButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_positive:
                currentFragment.onPositiveButtonClicked();
                break;
            case R.id.button_negative:
                currentFragment.onNegativeButtonClicked();
                break;
            case R.id.button_tertiary:
                currentFragment.onTertiaryButtonClicked();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        currentFragment.onBackPressed();
    }
}
