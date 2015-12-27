package org.theronin.expensetracker.pages.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.SupportedCurrencies;

public class SettingsFragment extends PreferenceFragment
        implements OnPreferenceChangeListener {

    public static final String TAG = SettingsFragment.class.getName();

    private ListPreference homeCurrencyPreference;
    private ListPreference currentCurrencyPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SupportedCurrencies supportedCurrencies = new SupportedCurrencies();
        CharSequence[] codes = supportedCurrencies.getCodes();
        CharSequence[] names = supportedCurrencies.getNames();

        homeCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_home_currency_key));
        homeCurrencyPreference.setEntries(names);
        homeCurrencyPreference.setEntryValues(codes);
        updateHomeCurrencyPreferenceSummary(homeCurrencyPreference.getValue());
        homeCurrencyPreference.setOnPreferenceChangeListener(this);

        currentCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_current_currency_key));
        currentCurrencyPreference.setEntries(names);
        currentCurrencyPreference.setEntryValues(codes);
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
