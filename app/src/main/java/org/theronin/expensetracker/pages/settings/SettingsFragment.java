package org.theronin.expensetracker.pages.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.text.Editable;
import android.text.TextWatcher;

import org.theronin.expensetracker.R;
import org.theronin.expensetracker.data.SupportedCurrencies;

public class SettingsFragment extends PreferenceFragment
        implements OnPreferenceChangeListener, TextWatcher {

    public static final String TAG = SettingsFragment.class.getName();

    private ListPreference homeCurrencyPreference;
    private ListPreference currentCurrencyPreference;

    private EditTextPreference monthlyLimitPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        homeCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_home_currency_key));
        setCurrencyPreference(homeCurrencyPreference);

        currentCurrencyPreference = (ListPreference) findPreference(getString(R.string.pref_current_currency_key));
        setCurrencyPreference(currentCurrencyPreference);

        monthlyLimitPreference = (EditTextPreference) findPreference(getString(R.string.pref_monthly_limit_key));
        monthlyLimitPreference.getEditText().addTextChangedListener(this);
        updateMonthlyLimitPreferenceSummary(monthlyLimitPreference.getText());
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

    private void updateMonthlyLimitPreferenceSummary(String newLimit) {
        String summary = Long.parseLong(newLimit) > 0 ?
                String.format(getString(R.string.pref_monthly_limit_summary), newLimit) :
                getString(R.string.pref_monthly_limit_summary_off);
        monthlyLimitPreference.setSummary(summary);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateMonthlyLimitPreferenceSummary(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
