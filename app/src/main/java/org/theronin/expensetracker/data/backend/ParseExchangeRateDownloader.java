package org.theronin.expensetracker.data.backend;

import android.support.annotation.NonNull;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;

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
    public void downloadExchangeRates(List<ExchangeRate> ratesToDownload) {
        Map<String, String> params = new HashMap<>();
        params.put("codes", createCodesObject(ratesToDownload));
        params.put("dates", createDatesObject(ratesToDownload));

        //TODO make sure that the ExchangeRates passed in get updated, rather than creating a new list
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
        return new ExchangeRate(-1,
                parseObject.getString("currency"),
                DateUtils.getUtcTimeFromStorageFormattedDate(parseObject.getString("date")),
                parseObject.getDouble("usdRate"),
                -1,
                0
        );
    }

    private String createCodesObject(List<ExchangeRate> rates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rates.size(); i++) {
            sb.append(rates.get(i).currencyCode);
            if (i < rates.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private String createDatesObject(List<ExchangeRate> rates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rates.size(); i++) {
            sb.append(DateUtils.getStorageFormattedDate(rates.get(i).utcDate));
            if (i < rates.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}
