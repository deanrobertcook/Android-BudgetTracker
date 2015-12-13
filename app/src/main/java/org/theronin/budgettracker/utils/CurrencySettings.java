package org.theronin.budgettracker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.theronin.budgettracker.model.Currency;

public class CurrencySettings implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context context;
    private Listener listener;

    private SharedPreferences defaultPreferences;
    private Currency homeCurrency;
    private Currency currentCurrency;

    public CurrencySettings(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;

        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        defaultPreferences.registerOnSharedPreferenceChangeListener(this);
        setCurrencies();
    }

    private void setCurrencies() {
        homeCurrency = MoneyUtils.getHomeCurrency(context, defaultPreferences);
        currentCurrency = MoneyUtils.getCurrentCurrency(context, defaultPreferences);
    }

    public Currency getHomeCurrency() {
        return homeCurrency;
    }

    public Currency getCurrentCurrency() {
        return currentCurrency;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setCurrencies();
        updateListener();
    }

    private void updateListener() {
        if (listener != null) {
            listener.onHomeCurrencyChanged(homeCurrency);
            listener.onCurrentCurrencyChanged(currentCurrency);
        }
    }

    public interface Listener {
        void onHomeCurrencyChanged(Currency homeCurrency);
        void onCurrentCurrencyChanged(Currency currentCurrency);
    }
}
