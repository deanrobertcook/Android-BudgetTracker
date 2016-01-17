package org.theronin.expensetracker.data.backend;

import android.support.annotation.NonNull;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParseExchangeRateDownloader implements ExchangeRateDownloader {

    private Callback callback;

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void downloadExchangeRates(Set<String> datesToDownload, Set<String> codesToDownload) {
        if (callback == null) {
            throw new IllegalStateException("A Callback must be set");
        }
        Map<String, String> params = new HashMap<>();
        params.put("dates", createDatesObject(datesToDownload));
        params.put("codes", createCodesObject(codesToDownload));

        try {
            List<ParseObject> parseObjects = ParseCloud.callFunction("exchangeRate", params);

            List<ExchangeRate> rates = new ArrayList<>();
            for (ParseObject parseObject : parseObjects) {
                ExchangeRate rate = getExchangeRate(parseObject);
                rates.add(rate);
            }
            callback.onDownloadComplete(rates);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private ExchangeRate getExchangeRate(ParseObject parseObject) {
        return new ExchangeRate(-1,
                parseObject.getString("currency"),
                DateUtils.getUtcTime(parseObject.getString("date")),
                parseObject.getDouble("usdRate"),
                -1,
                0
        );
    }

    private String createCodesObject(Set<String> codes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String code : codes) {
            sb.append(code);
            if (i < codes.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        return sb.toString();
    }

    private String createDatesObject(Set<String> dates) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String date : dates) {
            sb.append(date);
            if (i < dates.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        return sb.toString();
    }
}
