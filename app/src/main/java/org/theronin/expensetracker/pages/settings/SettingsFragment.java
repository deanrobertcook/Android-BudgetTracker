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

    private ListPreference homeCurrencyPreference;
    private ListPreference currentCurrencyPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        homeCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_home_currency_key));
        setCurrencyPreference(homeCurrencyPreference);

        currentCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_current_currency_key));
        setCurrencyPreference(currentCurrencyPreference);
    }

    private void setCurrencyPreference(ListPreference currencyPreference) {
        SupportedCurrencies supportedCurrencies = new SupportedCurrencies();
        CharSequence[] codes = supportedCurrencies.getCodes();
        CharSequence[] names = supportedCurrencies.getNames();

        currencyPreference.setEntries(names);
        currencyPreference.setEntryValues(codes);
        updateCurrencyPreferenceSummary(currencyPreference, currencyPreference.getValue());
        currencyPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(homeCurrencyPreference.getKey())) {
            updateCurrencyPreferenceSummary(preference, (String) newValue);
        } else if (preference.getKey().equals(currentCurrencyPreference.getKey())) {
            updateCurrencyPreferenceSummary(preference, (String) newValue);
        }
        return true;
    }

    private void updateCurrencyPreferenceSummary(Preference preference, String newValue) {
        String summary;
        if (preference.getKey().equals(homeCurrencyPreference.getKey())) {
            summary = String.format(getString(R.string.pref_home_currency_summary), newValue);
        } else {
            summary = String.format(getString(R.string.pref_current_currency_summary), newValue);
        }
        preference.setSummary(summary);
    }
}
