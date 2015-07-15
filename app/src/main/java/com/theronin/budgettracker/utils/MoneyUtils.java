package com.theronin.budgettracker.utils;

import java.util.Currency;
import java.util.Locale;

public class MoneyUtils {

    public static String getCurrencySymbol() {
        Locale locale = new Locale("en", "DE");
        Currency currency = Currency.getInstance(locale);
        return currency.getSymbol();
    }
}
