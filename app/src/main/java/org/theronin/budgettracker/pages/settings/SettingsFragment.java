package org.theronin.budgettracker.pages.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import org.theronin.budgettracker.R;

public class SettingsFragment extends PreferenceFragment
        implements OnPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getName();

    private ListPreference homeCurrencyPreference;
    private ListPreference currentCurrencyPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        homeCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_home_currency_key));
        updateHomeCurrencyPreferenceSummary(homeCurrencyPreference.getValue());
        homeCurrencyPreference.setOnPreferenceChangeListener(this);

        currentCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_current_currency_key));
        updateCurrentCurrencyPreferenceSummary(currentCurrencyPreference.getValue());
        currentCurrencyPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals(homeCurrencyPreference.getKey())) {
            updateHomeCurrencyPreferenceSummary((String) newValue);
        } else if (preference.getKey().equals(currentCurrencyPreference.getKey())) {
            updateCurrentCurrencyPreferenceSummary((String) newValue);
        }

        return true;
    }

    private void updateHomeCurrencyPreferenceSummary(String newValue) {
        String summary = String.format(getString(R.string.pref_home_currency_summary), newValue);
        homeCurrencyPreference.setSummary(summary);
    }

    private void updateCurrentCurrencyPreferenceSummary(String newValue) {
        String summary = String.format(getString(R.string.pref_current_currency_summary), newValue);
        currentCurrencyPreference.setSummary(summary);
    }
}
