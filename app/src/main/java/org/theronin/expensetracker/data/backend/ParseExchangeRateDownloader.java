package org.theronin.expensetracker.data.backend;

import android.support.annotation.NonNull;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.theronin.expensetracker.model.Currency;
import org.theronin.expensetracker.model.ExchangeRate;
import org.theronin.expensetracker.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseExchangeRateDownloader implements ExchangeRateDownloader {

    private final Callback callback;

    public ParseExchangeRateDownloader(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void downloadExchangeRates(List<Currency> currencies, List<Long> utcDates) {
        Map<String, String> params = new HashMap<>();
        params.put("codes", createCodesObject(currencies));
        params.put("dates", createDatesObject(utcDates));

        //TODO make synchronous
        ParseCloud.callFunctionInBackground("exchangeRate", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                if (e == null) {
                    List<ExchangeRate> rates = new ArrayList<>();
                    List<ParseObject> parseObjects = (ArrayList<ParseObject>) object;
                    for (ParseObject parseObject : parseObjects) {
                        ExchangeRate rate = getExchangeRate(parseObject);
                        rates.add(rate);
                    }
                    callback.onDownloadComplete(rates);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @NonNull
    private ExchangeRate getExchangeRate(ParseObject parseObject) {
        return new ExchangeRate(
                parseObject.getString("currency"),
                DateUtils.getUtcTimeFromStorageFormattedDate(parseObject.getString("date")),
                parseObject.getDouble("usdRate"),
                -1
        );
    }

    private String createCodesObject(List<Currency> currencies) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currencies.size(); i++) {
            sb.append(currencies.get(i).code);
            if (i < currencies.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private String createDatesObject(List<Long> dates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dates.size(); i++) {
            sb.append(DateUtils.getStorageFormattedDate(dates.get(i)));
            if (i < dates.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
