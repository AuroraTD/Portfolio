// TODO MAKE THIS A COMPREHENSIVE TEST
// TODO establish an array of pairs - URL & expected response (compare with stringify)

// Require
var https = require("https");

// Establish pairs of test URLs & expected responses
var testResponsePairs = [
    // Wrong endpoint
    {
        url:                "http://localhost:3000/",
        expectedResponse:   "Please use endpoint /value"
    },
    // Missing parameters
    {
        url:                "http://localhost:3000/value",
        expectedResponse:   {"status":"error","message":"missing parameter - value"}
    },
    {
        url:                "http://localhost:3000/value?value=10000",
        expectedResponse:   {"status":"error","message":"missing parameter - make"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota",
        expectedResponse:   {"status":"error","message":"missing parameter - model"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla",
        expectedResponse:   {"status":"error","message":"missing parameter - age"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=12",
        expectedResponse:   {"status":"error","message":"missing parameter - owners"}
    },
    // Insane parameters
    {
        url:                "http://localhost:3000/value?value=-10000&make=toyota&model=corolla&age=12&owners=1",
        expectedResponse:   {"status":"error","message":"initial value of car must be greater than zero"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=-12&owners=1",
        expectedResponse:   {"status":"error","message":"age of car (months) must be greater than or equal to zero"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=12&owners=-1",
        expectedResponse:   {"status":"error","message":"number of previous owners must be greater than or equal to zero"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=12&owners=1&mileage=-3000",
        expectedResponse:   {"status":"error","message":"mileage of car must be greater than or equal to zero"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=12&owners=1&mileage=3000&collisions=-1",
        expectedResponse:   {"status":"error","message":"number of collisions must be greater than or equal to zero"}
    },
    // Bad make & model combo
    {
        url:                "http://localhost:3000/value?value=10000&make=tiyiti&model=corolla&age=12&owners=1&mileage=3000&collisions=1",
        expectedResponse:   {"status":"error","message":"DOT did not return any models for the make 'tiyiti'"}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=cirilli&age=12&owners=1&mileage=3000&collisions=1",
        expectedResponse:   {"status":"error","message":"DOT did not return the model 'cirilli' for the make 'toyota'"}
    },
    // Owned by many people - final value 25% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=3",
        expectedResponse:   {"status":"success","value":7500}
    },
    // 10 months old - final value 5% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=10&owners=1",
        expectedResponse:   {"status":"success","value":9500}
    },
    // Very old - final value 60% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=1000&owners=1",
        expectedResponse:   {"status":"success","value":4000}
    },
    // 10k miles - final value 2% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=1&mileage=10000",
        expectedResponse:   {"status":"success","value":9800}
    },
    // Many miles - final value 30% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=1&mileage=1000000",
        expectedResponse:   {"status":"success","value":7000}
    },
    // 1 collision - final value 2% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=1&collisions=1",
        expectedResponse:   {"status":"success","value":9800}
    },
    // Many collisions - final value 10% lower than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=1&collisions=10",
        expectedResponse:   {"status":"success","value":9000}
    },
    // No previous owners - final value 10% higher than initial value
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=0",
        expectedResponse:   {"status":"success","value":11000}
    },
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=0&mileage=0",
        expectedResponse:   {"status":"success","value":11000}
    }
    ,
    {
        url:                "http://localhost:3000/value?value=10000&make=toyota&model=corolla&age=0&owners=0&mileage=0&collisions=0",
        expectedResponse:   {"status":"success","value":11000}
    }
    // All together now TODO ADD 3 TESTS WITH RANDOMLY CHOSEN PARAMETERS
];

// Check that make & model is valid
testURL = "https://vpic.nhtsa.dot.gov/api/vehicles/getmodelsformake/" + request.query.make + "?format=json";
https.get(testURL, function(testResponse) {

    // Declare variables
    var data;

    // Gather data returned from DOT API
    data = "";
    testResponse.on('data', function(chunk) {
        data += chunk;
    });

    // When all data returned, check for model
    testResponse.on('end', function() {

        // Declare variables
        var responseData;

        // Check response data for model
        responseData = JSON.parse(data);
        if (!responseData.Results) {
            errorMessage = "DOT did not return any models for the make '" + request.query.make + "'";
            return response.status(500).send({"status": "error", "message": errorMessage});
        }
        else {

        }
    });

}).on("error", function(error) {
    console.log(error.message);
});