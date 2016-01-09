package org.theronin.expensetracker.data;

import org.theronin.expensetracker.data.backend.SyncState;
import org.theronin.expensetracker.model.Category;
import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {

    public static final long DEFAULT_LATCH_WAIT = 2000;

    public static List<Entry> createEntries(int numEntries, boolean createGlobalId, SyncState syncState) {
        long someDate = System.currentTimeMillis();
        long someAmount = 100L;
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
