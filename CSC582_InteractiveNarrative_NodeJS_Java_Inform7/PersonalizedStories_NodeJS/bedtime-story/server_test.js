/*******************************************************************************************
 *
 * TEST for a simple Node.js REST server for a children's bedtime story generator
 *
 * TO RUN:  Open terminal in directory containing server.js
 *              Execute "npm install"
 *              Execute "node server.js"
 *          Open terminal in directory containing server_test.js
 *              Execute "node server_test.js"
 *
 *******************************************************************************************/

// Require
var http = require("http");

// Constants
const BASE_URL = "http://localhost:3001/";

// Establish pairs of test URLs & expected responses
var testResponsePairs = [
    // Story Setup - missing parameters
    {
        description:        "Story Setup (Missing Parameters)",
        url:                BASE_URL + "setup",
        responseIncludes:   ["error", "missing parameter - age"]
    },
    // Story Setup - should provide several prompts (each of which should have at least one option)
    {
        description:        "Story Setup (Correct Request)",
        url:                BASE_URL + "setup?age=6",
        responseIncludes:   ["promptName", "prompt", "options", "name", "value"]
    },
    // Story Generation - Missing parameters
    {
        description:        "Story Generation (Missing Parameters)",
        url:                BASE_URL + "generate",
        responseIncludes:   ["error", "missing parameter - age"]
    },
    // Story Generation - Insane parameters
    {
        description:        "Story Generation (Bad Parameters)",
        url:                BASE_URL + "generate?age=-9",
        responseIncludes:   ["error", "child's age must be greater than zero"]
    },
    {
        description:        "Story Generation (Bad Parameters)",
        url:                BASE_URL + "generate?age=0",
        responseIncludes:   ["error", "child's age must be greater than zero"]
    },
    // Story Generation - All optional parameters left up to chance
    // All expected response fields must be present
    // Must be at least one image
    // Story cannot be blank string
    {
        description:        "Story Generation (Optional Parameters Excluded)",
        url:                BASE_URL + "generate?age=7",
        responseIncludes:   ["setupOptions", "age", "boy", "girl", "animal", "color", "theme", "location", "images", "animals", "locations", "themes", ".jpg", "story", "The End"]
    },
    // Story Generation - All optional parameters included
    // All expected response fields must be present
    // Must be at least one image
    // Story cannot be blank string
    {
        description:        "Story Generation (Optional Parameters Included)",
        url:                BASE_URL + "generate?age=13&boy=Joe&girl=Sue&animal=bunny&color=red&theme=castle&location=africa",
        responseIncludes:   ["setupOptions", "age", "boy", "girl", "animal", "color", "theme", "location", "images", "animals", "locations", "themes", ".jpg", "story", "The End"]
    }
];

// Kick off testing of all URL-Response pairs
testOnePair();

// Method to test a single pair (calls itself when done)
function testOnePair () {

    // Declare constants
    const MAX_RESPONSE_MS = 2000;

    // Declare variables
    var nextPair;
    var timeStart_ms;

    // Prep for the next test
    nextPair = testResponsePairs.shift();
    timeStart_ms = new Date().getTime();

    // Test response (and response time)
    http.get(nextPair.url, function(testResponse) {

        // Declare variables
        var data;
        var timeEnd_ms;
        var timeElapsed_ms;

        // Gather data returned from test
        data = "";
        testResponse.on('data', function(chunk) {
            data += chunk;
        });

        // When all data returned, check for expected response
        testResponse.on('end', function() {
            timeEnd_ms = new Date().getTime();
            timeElapsed_ms = timeEnd_ms - timeStart_ms;
            console.log("\n\nTEST: " + nextPair.description + "\n");
            console.log("url", nextPair.url);
            console.log("expected response includes", JSON.stringify(nextPair.responseIncludes, null, 2));
            console.log("actual response", JSON.stringify(JSON.parse(data), null, 2));
            console.log("expected elapsed time (ms) <", MAX_RESPONSE_MS);
            console.log("actual elapsed time (ms)", timeElapsed_ms);
            if (
                // Expected response didn't include everything it was supposed to
                !nextPair.responseIncludes.every(function(includedString){
                    return data.includes(includedString);
                }) ||
                // Took too long
                timeElapsed_ms > MAX_RESPONSE_MS
            ) {
                console.log("TEST FAILED!\n");
            }
            else {
                console.log("test passed");
                if (testResponsePairs.length > 0) {
                    testOnePair();
                }
                else {
                    console.log("\n\nALL TESTS PASS!\n");
                }
            }
        });

    }).on("error", function(error) {
        console.log(error.message);
    });
}