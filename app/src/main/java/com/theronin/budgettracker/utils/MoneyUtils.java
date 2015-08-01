package com.theronin.budgettracker.utils;

import java.util.Currency;
import java.util.Locale;

public class MoneyUtils {

    public static String getCurrencySymbol() {
        Locale locale = new Locale("en", "DE");
        Currency currency = Currency.getInstance(locale);
        return currency.getSymbol();
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
}
