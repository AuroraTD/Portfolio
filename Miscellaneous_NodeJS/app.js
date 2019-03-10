/*******************************************************************************************
 * 
 * A simple Node.js REST server for returning the value of a used car
 * 
 * Used information from: 
 *  https://www.thepolyglotdeveloper.com/2015/10/create-a-simple-restful-api-with-node-js/
 * 
 * TO RUN:  Open terminal in directory containing app.js
 *          Run "npm install"
 *          Execute "node app.js"
 * 
 * TO TEST: TODO update these instructions for fully testing via browser AND via JSON
 *          Open a browser
 *          Navigate to localhost:3000
 *******************************************************************************************/

// Require
var express = require("express");
var bodyParser = require("body-parser");

// Create app
var app = express();

// Allow JSON or URL values
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

// Establish routes
var routes = require("./routes/routes.js")(app);

// Listen
var server = app.listen(3000, function(){
    console.log("Listening on port %s...", server.address().port);
});