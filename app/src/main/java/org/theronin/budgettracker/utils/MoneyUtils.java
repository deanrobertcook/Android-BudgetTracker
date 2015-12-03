package org.theronin.budgettracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.theronin.budgettracker.R;
import org.theronin.budgettracker.model.Currency;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;

public class MoneyUtils {

    public static Currency getHomeCurrency(Context context, SharedPreferences defaultPreferences) {
        return getCurrency(R.string.pref_home_currency_key, context, defaultPreferences);
    }

    public static Currency getCurrentCurrency(Context context, SharedPreferences defaultPreferences) {
        return getCurrency(R.string.pref_current_currency_key, context, defaultPreferences);
    }

    public static Currency getCurrency(int currencyKeyResourceId,
                                       Context context,
                                       SharedPreferences defaultPreferences) {
        String defaultCurrency = context.getString(R.string.pref_currency_default);
        String currentCurrencyKey = context.getString(currencyKeyResourceId);

        String[] currencyCodes = context.getResources().getStringArray(R.array.currency_codes);
        String[] currencySymbols = context.getResources().getStringArray(R.array.currency_symbols);

        String currencyCode = defaultPreferences.getString(currentCurrencyKey, defaultCurrency);
        int index = Arrays.asList(currencyCodes).indexOf(currencyCode);

        String currencySymbol = currencySymbols[index];
        return new Currency(currencyCode, currencySymbol);
    }

    public static long convertToCents(String amount) {
        String[] parts = amount.split("\\.");

        long dollars = Long.parseLong(parts[0]);
        long cents = 0;

        if (parts.length == 2) {
            String centsStr = parts[1];
            cents = Long.parseLong(centsStr);
            if (centsStr.length() == 1) {
                cents = cents * 10;
            }
        }

        return dollars * 100 + cents;
    }

    public static String convertToDollars(long amount) {
        if (amount < 0) {
            return "-.--";
        }
        long cents = amount % 100;
        String centsStr = Long.toString(cents);
        if (cents < 10) {
            centsStr = "0" + centsStr;
        }

        long dollars = amount / 100;
        return Long.toString(dollars) + "." + centsStr;
    }

    public static String convertCentsToDisplayAmount(long cents){
        if (cents < 0) {
            return "-.--";
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setGroupingUsed(true);

        BigDecimal parsed = new BigDecimal(Long.toString(cents)).setScale(2,BigDecimal.ROUND_FLOOR)
                .divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        return numberFormat.format((parsed));
    }

    public static long convertDisplayAmountToCents(String displayAmount) {
        double result = 0;
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);

        try {
            result = numberFormat.parse(displayAmount).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return (long) (Math.round(result * 100));
    }

}
