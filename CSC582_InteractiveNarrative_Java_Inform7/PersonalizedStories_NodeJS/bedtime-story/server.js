/*******************************************************************************************
 *
 * A simple Node.js REST server for a children's bedtime story generator
 *
 * TO RUN:  Open terminal in directory containing server.js
 *          Execute "npm install"
 *          Execute "node server.js"
 *
 * TO TEST:
 *
 *  MANUAL:     Open a browser
 *              To get story set-up options:
 *                  Navigate to http://localhost:3001/setup
 *              To generate a story:
 *                  Navigate to http://localhost:3001/generate?age=[x]&animal=[x]&color=[x]&theme=[x]&location=[x]
 *                  (replace each [x] with the value of your choice)
 *
 *  AUTOMATED:  Open terminal in directory containing server_test.js
 *              Execute "node server_test.js"
 *
 *******************************************************************************************/

// Require
var express = require("express");
var bodyParser = require("body-parser");

// Create app
var app = express();

// Allow JSON or URL values
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

// To allow Angular app to make requests of this server
// Per https://enable-cors.org/server_expressjs.html
app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
});

// Serve image assets
app.use(express.static(__dirname + '/src/assets/images'));

// Establish routes
var routes = require("./server_routes.js")(app);

// Listen (NOT on 3000, that might be the angular default port)
var server = app.listen(3001, function(){
    console.log("%s Listening on port %s...", new Date(), server.address().port);
});