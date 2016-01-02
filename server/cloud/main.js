Parse.Cloud.beforeSave("Entry", function(request, response) {
  if (!request.object.get("isDeleted")) {
    request.object.set("isDeleted", false);
  }

  response.success();
});

//Request expects a currency code and a comma separated list of dates of the form YYYY-MM-DD
Parse.Cloud.define("exchangeRate", function(request, response) {
    var ExchangeRate = Parse.Object.extend("ExchangeRate");

    var dateStrings = request.params.dates.split(",");

    function getDateFromString(dateString) {
        var startDateParts = startDateString.split("-");
        var startDate = new Date(startDateParts[0], startDateParts[1], startDateParts[2]);
    }

    var startDateString = dateStrings[0];


    var code = request.params.code;

    for (var dateString in dateStrings) {
        var dateParts = dateString.split("-");
        var date = new Date(dateParts[0], dateParts[1], dateParts[2]);

        var query = new Parse.Query(ExchangeRate);
        query.equalTo("code", code);
    }

});


//A job to be run daily to fetch the exchange rate data
Parse.Cloud.job("dailyExchangeRate", function(request, status) {
    Parse.Cloud.httpRequest({
        url: 'https://openexchangerates.org/api/latest.json',
        params: {
            app_id : '30b554493a094428adcdff4abb4d2c7b'
        }
    }).then(function(httpResponse) {
        //TODO check date before continuing (don't bother if we already have today's rates);
        //TODO also, log this say I can track when the API rates are released each day
        var responseJson = JSON.parse(httpResponse.text);
        var rates = responseJson.rates;
        var exchangeRates = [];
        for (var code in rates) {
            if (rates.hasOwnProperty(code)) {
                var rate = rates[code];
                var exchangeRate = new Parse.Object("ExchangeRate");
                exchangeRate.set("currency", code);
                exchangeRate.set("usdRate", rate);

                exchangeRates.push(exchangeRate);
            }
        }

        Parse.Object.saveAll(exchangeRates, {
            success: function(list) {
                console.log(list.length + " items saved");
                status.success("Job completed successfully");
            },
            error: function(error) {
                status.error("Job did not complete successfully");
            }
        });
    }, function (httpResponse) {
        console.log("Request failed: " + httpResponse.status);
        status.error("Job did not complete successfully");
    });
});