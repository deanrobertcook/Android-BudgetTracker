package org.theronin.expensetracker.testutils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.theronin.expensetracker.model.Entry;
import org.theronin.expensetracker.model.ExchangeRate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.argThat;

public class MockitoMatchers {

    /**
     * Check that two lists of entries are the same, where each entry has to match by its usual
     * sense of equality, plus the sync states and global ids must match - since this matcher
     * will be used mostly to check sync states.
     * @param expectedList
     * @return a mockito matcher for comparing the two lists.
     */
    public static List<Entry> containsAllEntries(final List<Entry> expectedList) {
        return argThat(new TypeSafeMatcher<List<Entry>>() {

            public List<Entry> actualList;

            @Override
            protected boolean matchesSafely(List<Entry> actualList) {
                this.actualList = actualList;
                if (expectedList.size() != actualList.size()) {
                    return false;
                }

                Collections.sort(expectedList);
                Collections.sort(actualList);

                for (int i = 0; i < expectedList.size(); i++) {
                    Entry expected = expectedList.get(i);
                    Entry actual = actualList.get(i);

                    if (!expected.equals(actual) ||
                            expected.getGlobalId() != actual.getGlobalId() ||
                            expected.getSyncState() != actual.getSyncState()) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

                if (expectedList.size() != actualList.size()) {
                    description.appendText(String.format(
                            "The size of the two lists did not match. expected: %d, actual: %d\n",
                            expectedList.size(), actualList.size()));
                }
                description.appendValue(expectedList);
                description.appendText("\n");
                description.appendValue(actualList);
            }
        });
    }

    /**
     * A matcher that takes two lists of Exchange rates and compares them by date (of the form
     * YYYY-MM-DD, currency code, usdRate, the last download attempt (within the nearest second) and
     * the number of counted download attempts.
     *
     * @param expectedList
     * @return
     */
    public static List<ExchangeRate> containsAllExchangeRates(final List<ExchangeRate> expectedList) {
        return argThat(new TypeSafeMatcher<List<ExchangeRate>>() {

            public List<ExchangeRate> actualList;
            private final double RATE_EPSILON = 0.000001;

            @Override
            protected boolean matchesSafely(List<ExchangeRate> actualList) {
                this.actualList = actualList;
                if (expectedList.size() != actualList.size()) {
                    return false;
                }

                Collections.sort(expectedList);
                Collections.sort(actualList);

                for (int i = 0; i < expectedList.size(); i++) {
                    ExchangeRate expected = expectedList.get(i);
                    ExchangeRate actual = actualList.get(i);

                    long timeDiff = Math.abs(expected.getUtcLastUpdated() - actual.getUtcLastUpdated());

                    if (!expected.equals(actual) ||
                            expected.getDownloadAttempts() != actual.getDownloadAttempts() ||
                            timeDiff > 1000 ||
                            Math.abs(expected.getUsdRate() - actual.getUsdRate()) > RATE_EPSILON) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

                if (expectedList.size() != actualList.size()) {
                    description.appendText(String.format(
                            "The size of the two arrays did not match. expected: %d, actual: %d\n",
                            expectedList.size(), actualList.size()));
                }
                description.appendValue(expectedList);
                description.appendText("\n");
                description.appendValue(actualList);
            }
        });
    }

    public static Set<String> setContainsAll(final Set<String> expectedSet) {
        return argThat(new TypeSafeMatcher<Set<String>>() {

            private Set<String> actualDates;

            @Override
            protected boolean matchesSafely(final Set<String> actualSet) {
                this.actualDates = actualSet;
                return expectedSet.equals(actualSet);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Sets don't match");
                description.appendValue(expectedSet);
                description.appendValue(actualDates);
            }
        });
    }
}
