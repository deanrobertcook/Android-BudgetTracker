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

//TODO create a background job to download exchange rates every day - only really needs to be done from point of release.

//Request contains a list of currency codes and dates of the form YYYY-MM-DD, both comma separated
Parse.Cloud.define("exchangeRate", function(request, response) {
    var ExchangeRate = Parse.Object.extend("ExchangeRate");

    dateStrings = request.params.dates.split(",").filter(function(item, pos, self) {
        return self.indexOf(item) == pos;
    });
    var codes = request.params.codes.split(",");

    var query = new Parse.Query(ExchangeRate);
    query.containedIn("currency", codes);
    query.containedIn("date", dateStrings);
    query.find().then(function(results) {
        var promise = Parse.Promise.as();
        var datesRetrieved = [];
        var i = 0;
        for (i; i < results.length; i++) {
            var objDate = results[i].get("date");
            datesRetrieved.push(objDate);
        }

        var missing = dateStrings.filter(function(obj) { return datesRetrieved.indexOf(obj) == -1; });
        console.log("Missing: " + missing);

        if (missing.length > 0) {
            console.log("missing " + missing.length + " exchange rates");

            _.each(missing, function(dateString) {
                console.log("downloading for date : " + dateString);
                promise = promise.then(function() {
                    return Parse.Cloud.httpRequest({
                        url: 'https://openexchangerates.org/api/historical/' + dateString + '.json',
                        params: {
                            app_id : '30b554493a094428adcdff4abb4d2c7b'
                        }
                    }).then(function(httpResponse) {
                        var responseJson = JSON.parse(httpResponse.text);
                        console.log("timestamp: " + responseJson.timestamp);
                        var rates = responseJson.rates;
                        console.log(rates);
                        var exchangeRates = [];
                        for (var code in rates) {
                            if (rates.hasOwnProperty(code)) {
                                var rate = rates[code];
                                var exchangeRate = new Parse.Object("ExchangeRate");
                                exchangeRate.set("currency", code);
                                exchangeRate.set("usdRate", rate);
                                exchangeRate.set("date", dateString);
                                exchangeRates.push(exchangeRate);
                            }
                        }
                        console.log("saving " + exchangeRates.length + " exrates");
                        return Parse.Object.saveAll(exchangeRates);
                    }, function(httpResponse) {
                        console.log(httpResponse);
                        return Parse.Promise.as("Ignore missing dates");
                    });
                });
            });
        }
        return promise;
    }).then(function() {
        query.find().then(function (results) {
            response.success(results);
        }, function(error) {
            response.error(error);
        });
    }, function(error) {
        response.error(error);
    });
});


