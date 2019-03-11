// TODO add block comment here
// TODO reduce level of indentation
// TODO truncate final value to 2 decimal places

// Require
var https = require("https");

// Define Routes
var appRouter = function (app) {

    /* VALUE
        Required query parameters:
            VALUE -         Initial value of car, in dollars
            MAKE -          e.g. "Toyota"
            MODEL -         e.g. "Corolla"
            AGE -           Age of car, in months
            OWNERS -        Number of previous owners
        Optional query parameters:
            MILEAGE -       Miles on the car
            COLLISIONS -    Number of collisions the car has been in
    */
    app.get("/value", function (request, response) {

        // Declare constants
        const REQUIRED_QUERY_PARAMS = ["value", "make", "model", "age", "owners"];

        // Declare variables
        var dotURL;
        var errorMessage;
        var i;

        // Check for required query parameters
        errorMessage = "";
        for (i = 0; i < REQUIRED_QUERY_PARAMS.length; i++) {
            if (!request.query[REQUIRED_QUERY_PARAMS[i]] || request.query[REQUIRED_QUERY_PARAMS[i]].length === 0) {
                errorMessage = "missing parameter - " + REQUIRED_QUERY_PARAMS[i];
                break;
            }
        }

        // Check for query parameter sanity
        if (errorMessage.length === 0) {
            if (Number(request.query.value) <= 0) {
                errorMessage = "initial value of car must be greater than zero";
            }
            else if (Number(request.query.age) < 0) {
                errorMessage = "age of car (months) must be greater than or equal to zero";
            }
            else if (Number(request.query.owners) < 0) {
                errorMessage = "number of previous owners must be greater than or equal to zero";
            }
            else if (request.query.mileage && Number(request.query.mileage) < 0) {
                errorMessage = "mileage of car must be greater than or equal to zero";
            }
            else if (request.query.collisions && Number(request.query.collisions) < 0) {
                errorMessage = "number of collisions must be greater than or equal to zero";
            }
        }

        // Reply with error if there was a problem
        if (errorMessage.length > 0) {
            return response.status(500).send({"status": "error", "message": errorMessage});
        }
        else {

            // Check that make & model is valid
            dotURL = "https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformake/" + request.query.make + "?format=json";
            https.get(dotURL, function(dotResponse) {

                // Declare variables
                var data;

                // Gather data returned from DOT API
                data = "";
                dotResponse.on('data', function(chunk) {
                    data += chunk;
                });

                // When all data returned, check for model
                dotResponse.on('end', function() {

                    // Declare contants
                    const MAX_AGE_YEARS = 10;
                    const MAX_AGE_MONTHS = MAX_AGE_YEARS * 12;
                    const MAX_MILEAGE = 150000;
                    const MAX_COLLISIONS = 5;

                    // Declare variables
                    var responseData;
                    var modelFound;
                    var carValue_dollars;
                    var valueAdjustment_percent;
                    var ageUsedForAdjustment;
                    var mileageUsedForAdjustment;
                    var collisionsUsedForAdjustment;

                    // Check response data for model
                    responseData = JSON.parse(data);
                    if (!responseData.Results || responseData.Results.length === 0) {
                        errorMessage = "DOT did not return any models for the make '" + request.query.make + "'";
                        return response.status(500).send({"status": "error", "message": errorMessage});
                    }
                    else {
                        modelFound = false;
                        for (i = 0; i < responseData.Results.length; i++) {
                            if (responseData.Results[i].Model_Name.toLowerCase() === request.query.model.toLowerCase()) {
                                modelFound = true;
                                break;
                            }
                        }
                        if (!modelFound) {
                            errorMessage = "DOT did not return the model '" + request.query.model + "' for the make '" + request.query.make + "'";
                            return response.status(500).send({"status": "error", "message": errorMessage});
                        }
                        else {

                            // Calculate true value of car
                            carValue_dollars = request.query.value;
                            valueAdjustment_percent = 0;

                            // Adjust for owners (if more than 2 previous owners - apply to initial value)
                            if (Number(request.query.owners) > 2) {
                                carValue_dollars *= 0.75;
                            }

                            // Adjust for age
                            ageUsedForAdjustment = Math.min(MAX_AGE_MONTHS, Number(request.query.age));
                            valueAdjustment_percent -= ageUsedForAdjustment * 0.5;

                            // Adjust for mileage (optional parameter)
                            if (request.query.mileage) {
                                mileageUsedForAdjustment = Math.min(MAX_MILEAGE, Number(request.query.mileage));
                                valueAdjustment_percent -= (mileageUsedForAdjustment / 1000) * 0.2;
                            }

                            // Adjust for collisions (optional parameter)
                            if (request.query.collisions) {
                                collisionsUsedForAdjustment = Math.min(MAX_COLLISIONS, Number(request.query.collisions));
                                valueAdjustment_percent -= collisionsUsedForAdjustment * 2;
                            }

                            // Apply adjustments
                            carValue_dollars *= (1 + (valueAdjustment_percent / 100));

                            // Adjust for owners (if no previous owners - apply to final value)
                            if (Number(request.query.owners) === 0) {
                                carValue_dollars *= 1.1;
                            }

                            // Respond with the adjusted value of the car
                            return response.status(200).send({"status": "success", "value": carValue_dollars});

                        }
                    }
                });

            }).on("error", function(error) {
                return response.status(500).send({"status": "error", "message": error.message});
            });

        }

    });

}

// Export
module.exports = appRouter;