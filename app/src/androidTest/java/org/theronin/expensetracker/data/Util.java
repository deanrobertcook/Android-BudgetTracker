package org.theronin.expensetracker.data;

import org.theronin.expensetracker.data.backend.entry.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    public static List<Entry> createEntries(int numEntries, boolean createGlobalId, SyncState syncState) {
        long someDate = someDateAfter2000();
        long someAmount = someAmountUnder(100);
        Category someCategory = new Category("Test");
        //Needs to be one of the supported currencies
        Currency someCurrency = new Currency("AUD");
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < numEntries; i++) {
            entries.add(new Entry(
                    createGlobalId ? generateRandomGlobalId() : null,
                    syncState,
                    someDate,
                    someAmount,
                    someCategory,
                    someCurrency));
        }

        return entries;
    }

    public static long someDateAfter2000() {
        Random random = new Random();
        return random.nextLong() + 946681200000L; //Any date after 2000-01-01
    }

    public static long someAmountUnder(int amount) {
        Random random = new Random();
        return random.nextInt(amount);
    }

    private static final char[] symbols;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    public static String generateRandomGlobalId() {
        int length = 12;
        char[] stringAsChars = new char[length];
        Random random = new Random();
        for (int i = 0; i < stringAsChars.length; i++) {
            stringAsChars[i] = symbols[random.nextInt(symbols.length)];
        }
        return String.copyValueOf(stringAsChars);
    }
}
