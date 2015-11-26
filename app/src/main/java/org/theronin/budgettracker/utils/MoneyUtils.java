package org.theronin.budgettracker.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
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
