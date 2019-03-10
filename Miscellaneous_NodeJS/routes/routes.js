// TODO add block comment here
// TODO add plenty of comments in code as well

// Define Routes
var appRouter = function (app) {

    /* HOME
        Nothing supported at "/" so just tell the visitor what endpoint is supported
    */
    app.get("/", function (request, response) {
        response.send("Please use endpoint /value");
    });

    /* VALUE
        Required query parameters:
            VALUE, MAKE, MODEL, AGE, and OWNERS
        Optional query parameters:
            MILEAGE and COLLISIONS
    */
    app.get("/value", function (request, response) {
        // TODO populate
    });

}

// Export
module.exports = appRouter;