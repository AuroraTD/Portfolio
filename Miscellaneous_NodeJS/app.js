/*******************************************************************************************
 * 
 * A simple Node.js REST server for returning the value of a used car
 * 
 * TO RUN:  Open terminal in directory containing app.js
 *          Execute "npm install"
 *          Execute "node app.js"
 * 
 * TO TEST: Open a browser
 *          TODO complete test instructions (manually / automated)
 *          TODO methodically test every part of the requirements
 *          TODO methodically test everything that could be messed up in routes.js
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