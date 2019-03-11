A simple Node.js REST server for returning the value of a used car

TO RUN:  Open terminal in directory containing app.js
         Execute "npm install"
         Execute "node app.js"

TO TEST: 

 MANUAL:     Open a browser
             Navigate to http://localhost:3000/value?value=[x]&make=[x]&model=[x]&age=[x]&owners=[x]&mileage=[x]&collisions=[x]
             (replace each [x] with the value of your choice)

 AUTOMATED:  Open terminal in directory containing test.js
             Execute "node test.js"
             (created a simple roll-your-own test instead of Mocha or similar due to time constraints)