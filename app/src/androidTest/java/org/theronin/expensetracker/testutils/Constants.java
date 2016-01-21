package org.theronin.expensetracker.testutils;

import org.theronin.expensetracker.utils.DateUtils;

public class Constants {

    public static final long JAN_1_2000 = DateUtils.getUtcTime("2000-01-01");
    public static final long JAN_2_2000 = DateUtils.getUtcTime("2000-01-02");
    public static final long JAN_3_2000 = DateUtils.getUtcTime("2000-01-03");
    public static final long FEB_1_2000 = DateUtils.getUtcTime("2000-02-01");
    public static final long JAN_1_2011 = DateUtils.getUtcTime("2011-01-01");


    /**
     * These rates have been taken manually from openexchangerate.org.
     */
    public static final double EUR_JAN_1_2000 = 0.993161;
    public static final double AUD_JAN_1_2000 = 1.532914;
    public static final double AUD_FEB_1_2000 = 1.579927;
    public static final int COUNT_RATES_JAN_1_2000 = 43;
    public static final int COUNT_RATES_FEB_1_2000 = 59;
}
