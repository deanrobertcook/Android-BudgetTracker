var _ = require('underscore');

Parse.Cloud.beforeSave("Entry", function(request, response) {
  if (!request.object.get("isDeleted")) {
    request.object.set("isDeleted", false);
  }

  response.success();
});

Parse.Cloud.define("sendChangePush", function(request, response) {
    console.log(request);
    Parse.Push.send({
      channels: [ request.user.id ],
      data: {
        alert: "deviceShouldUpdate",
        installationId: request.params.installationId
      }
    }, {
      success: function() {
        response.success();
        // Push was successful
      },
      error: function(error) {
        response.error();
        // Handle error
      }
    });
});

Parse.Cloud.job("dailyRate", function(request, status) {
    var date = getDate();
    return Parse.Promise.when(buildDownloadPromises([date]));
});

function getDate() {
    var date = new Date();
    var yyyy = date.getFullYear().toString();
    var mm = (date.getMonth()+1).toString();
    var dd  = date.getDate().toString();
    var mmChars = mm.split('');
    var ddChars = dd.split('');
    return yyyy + '-' + (mmChars[1]?mm:"0"+mmChars[0]) + '-' + (ddChars[1]?dd:"0"+ddChars[0]);
}

//Request contains a list of currency codes and dates of the form YYYY-MM-DD, both comma separated
Parse.Cloud.define("exchangeRate", function(request, response) {
	console.log("exchangeRate called");
	var query = getExchangeRatesQuery(request.params.dates, request.params.codes);
    return query.find()
    .then(function(results) {
        return Parse.Promise.when(buildDownloadPromises(getMissingDates(results)))
    }).then(function() {
        return query.find();
    }).then(function (results) {
        console.log("All downloads successful");
        response.success(results);
    }, function(error) {
        console.log("Some error: " + error);
        response.error(error);
    });
});

function getExchangeRatesQuery(dates, codes) {
    var ExchangeRate = Parse.Object.extend("ExchangeRate");

    dateStrings = dates.split(",").filter(function(item, pos, self) {
        return self.indexOf(item) == pos;
    }).sort().reverse();
    var codes = codes.split(",");

    var query = new Parse.Query(ExchangeRate);
    query.containedIn("currency", codes);
    query.containedIn("date", dateStrings);
	return query;
}

function getMissingDates(resultsSaved) {
    //MAX_DOWNLOADS * (~180) < MINUTE_RATE_LIMIT
    var MAX_DOWNLOADS = 10;
    var datesRetrieved = [];
    var i = 0;
    for (i; i < resultsSaved.length; i++) {
        var objDate = resultsSaved[i].get("date");
        datesRetrieved.push(objDate);
    }
    return dateStrings
        .filter(function(obj) { return datesRetrieved.indexOf(obj) == -1; })
        .sort()
        .reverse()
        .slice(0, MAX_DOWNLOADS);
}

function buildDownloadPromises(missingDates) {
    return _.map(missingDates, function(dateString) {
        return Parse.Cloud.httpRequest({
            url: 'https://openexchangerates.org/api/historical/' + dateString + '.json',
            params: {
                app_id : '30b554493a094428adcdff4abb4d2c7b'
            }
        }).then(function(httpResponse) {
            return Parse.Object.saveAll(
                buildExchangeRates(JSON.parse(httpResponse.text).rates, dateString));
        });
    });
}

function buildExchangeRates(rates, dateString) {
    var exchangeRates = [];
    for (var code in rates) {
        if (rates.hasOwnProperty(code)) {
            exchangeRates.push(buildExchangeRate(code, rates[code], dateString));
        }
    }
    return exchangeRates;
}

function buildExchangeRate(code, rate, dateString) {
    var exchangeRate = new Parse.Object("ExchangeRate");
    exchangeRate.set("currency", code);
    exchangeRate.set("usdRate", rate);
    exchangeRate.set("date", dateString);
    return exchangeRate;
}


