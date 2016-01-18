package org.theronin.expensetracker.testutils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.ExchangeRateUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.argThat;

public class MockitoMatchers {

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

            private final double RATE_EPSILON = 0.000001;
            private boolean sizeDiffers = false;
            private int expectedSize = 0;
            private int actualSize = 0;
            private ExchangeRate expected;
            private ExchangeRate actual;

            @Override
            protected boolean matchesSafely(List<ExchangeRate> actualList) {
                if ((expectedSize = expectedList.size()) != (actualSize = actualList.size())) {
                    sizeDiffers = true;
                    return false;
                }
                Collections.sort(expectedList, ExchangeRateUtils.comparator());
                Collections.sort(actualList, ExchangeRateUtils.comparator());

                for (int i = 0; i < expectedList.size(); i++) {
                    ExchangeRate expected = expectedList.get(i);
                    ExchangeRate actual = actualList.get(i);

                    long timeDiff = Math.abs(expected.getUtcLastUpdated() - actual.getUtcLastUpdated());

                    if (!expected.equals(actual) ||
                            expected.getDownloadAttempts() != actual.getDownloadAttempts() ||
                            timeDiff > 1000 ||
                            Math.abs(expected.getUsdRate() - actual.getUsdRate()) > RATE_EPSILON) {

                        this.expected = expected;
                        this.actual = actual;
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {

                if (sizeDiffers) {
                    description.appendText(String.format(
                            "The size of the two arrays did not match. expected: %d, actual: %d",
                            expectedSize, actualSize
                    ));
                    return;
                }

                description.appendText(String.format(
                        "The first encountered elements that differed were (expected, actual): \n %s \n\t %s",
                        expected.toString(), actual.toString()
                ));
            }
        });
    }

    public static Set<String> setSizeMatches(final int expectedSize) {
        return argThat(new TypeSafeMatcher<Set<String>>() {
            int actualSize = 0;

            @Override
            protected boolean matchesSafely(Set<String> actualSet) {
                actualSize = actualSet.size();
                return expectedSize == actualSet.size();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("Sizes don't match: expected: %s, actual: %s",
                        expectedSize, actualSize));
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
