/*******************************************************************************************
 *
 * ROUTES for a simple Node.js REST server for a children's bedtime story generator
 *
 *******************************************************************************************/

// REQUIRES
const tracery = require('tracery-grammar');
const util = require('util');
const fs = require('fs');
const axios = require('axios');
const convert = require('color-convert');
const readdir = util.promisify(fs.readdir);

// CONSTANTS
const IMAGES_DIRECTORY = "./src/assets/images/";
const IMAGE_SERVE_URL = "http://localhost:3001/";
const STORY_PROMPTS = ["boy", "girl", "animal", "color", "theme", "location"];
const MIN_AREA_COUNTRY = 100000;
const MAX_AREA_COUNTRY = MIN_AREA_COUNTRY * 1000;

// DO FILE SYSTEM MINING ONCE (time consuming)
// This variable will be used as a singleton
var imageAssets;

// ROUTES
var appRouter = function (app) {

    app.get("/setup", storySetupRoute);
    app.get("/generate", storyGenerateRoute);

};

// ROUTE METHODS

/* STORY SET-UP ROUTE
    Arguments - request -   The HTTP request
                            Required query parameters:
                                AGE -       The age of the child
                            Optional query parameters:
                                None
                response -  Handle to HTTP response
    Return -    response -  JSON object containing:
                            Array of JSON objects
                            each describes some prompt for information from the user
                            each contains:
                            promptName -    The name of the prompt
                                            this is the name that should be used as a
                                            query parameter when generating a story
                            prompt -        A user friendly message that can be displayed to
                                            prompt the user to select an option of this type
                            options -       Not present for every prompt
                                            An array of JSON objects
                                            each describes one of the options that
                                            the user can select for this prompt
                                            each contains:
                                            name -  The name of the option (can be used for onscreen display)
                                            value - url OR hex (for images and colors respectively)
*/
async function storySetupRoute (request, response) {

    try {

        // Declare constants
        const REQUIRED_QUERY_PARAMS = ["age"];

        // Declare variables
        var promptPromises;
        var setupPrompts;
        var allOptions;
        var promptToDisplay;
        var age;
        var i;

        // Check for required query parameters (error thrown in case of problems)
        checkQueryParameters(request.query, REQUIRED_QUERY_PARAMS);

        // Save child's age for convenience
        age = request.query.age;

        // Perform time-consuming tasks in parallel
        promptPromises = STORY_PROMPTS.map(function(prompt){
            return getOptionObjects(prompt + "s", age);
        });
        allOptions = await Promise.all(promptPromises);

        // Generate the story set-up options
        // Do not include prompt for age
        // Front end developer specifically requested that we do not include age prompt
        setupPrompts = [];
        for (i = 0; i < STORY_PROMPTS.length; i++) {
            if (STORY_PROMPTS[i] == "location") {
                promptToDisplay = "Which " + STORY_PROMPTS[i] + " do you like the best?";
            }
            else if (STORY_PROMPTS[i] === "boy" || STORY_PROMPTS[i] === "girl") {
                promptToDisplay = "Which " + STORY_PROMPTS[i] + "'s name do you like the best?";
            }
            else {
                promptToDisplay = "Which " + STORY_PROMPTS[i] + " do you like the best?";
            }
            setupPrompts.push({
                promptName: STORY_PROMPTS[i],
                prompt:     promptToDisplay,
                options:    allOptions[i]
            });
        }

        // Provide story setup prompts in random order, more for older kids
        setupPrompts.sort(function(a,b){
            return Math.random() >= 0.5;
        });
        return response.status(200).send(setupPrompts.slice(0,ageToNumberOfOptions(age)));

    }
    catch (err) {
        return handleError(err, response);
    }

}

/* STORY GENERATION ROUTE
    Arguments - request -   The HTTP request
                            Required query parameters:
                                AGE -       The age of the child
                            Optional query parameters:
                                ANIMAL -    An animal in the story
                                COLOR -     The child's favorite color
                                THEME -     The theme of the story
                                LOCATION -  The geographical location of the story
                response -  Handle to HTTP response
    Return -    response -  JSON object containing:
                            setupOptions -  JSON object
                                            each key is the name of a setup prompt
                                            each value is the user-selected or randomly-selected
                                            option that was chosen for that prompt
                            images -        Array of URLS appropriate to display with the story
                                            These are returned in random order
                            story -         A string value containing the text
                                            of the generated children's story
*/
async function storyGenerateRoute (request, response) {

    try {

        // Declare constants
        const REQUIRED_QUERY_PARAMS = ["age"];
        const OPTIONAL_QUERY_PARAMS = STORY_PROMPTS;

        // Declare variables
        var options;
        var missingParamNames;
        var missingParamPromises;
        var missingParamOptions;
        var selectedOption;
        var age;
        var i;

        // Check for required query parameters (error thrown in case of problems)
        checkQueryParameters(request.query, REQUIRED_QUERY_PARAMS);

        // Save child's age for convenience
        age = request.query.age;

        // Randomly select for any optional parameters that are not provided by the user
        missingParamNames = [];
        missingParamPromises = [];
        for (i = 0; i < OPTIONAL_QUERY_PARAMS.length; i++) {
            if (!request.query[OPTIONAL_QUERY_PARAMS[i]] || request.query[OPTIONAL_QUERY_PARAMS[i]].length === 0) {
                missingParamNames.push(OPTIONAL_QUERY_PARAMS[i]);
                missingParamPromises.push(getOptionObjects(OPTIONAL_QUERY_PARAMS[i] + "s", age));
            }
        }

        // Finding the options for missing params is time-consuming so do in parallel
        missingParamOptions = await Promise.all(missingParamPromises);

        // Now that we have the options, use them
        for (i = 0; i < missingParamNames.length; i++) {
            options = missingParamOptions[i];
            if (options.length > 0) {
                selectedOption = options[Math.floor(Math.random() * options.length)];
                request.query[missingParamNames[i]] = selectedOption.name;
            }
        }

        // Generate the images & story
        storyContent = await generateStoryContent(request.query);

        // Return the generated story
        return response.status(200).send({
            setupOptions:   request.query,
            images:         storyContent.images,
            story:          storyContent.text
        });

    }
    catch (err) {
        return handleError(err, response);
    }

}

// EXPORTS
module.exports = appRouter;

// HELPER METHODS

/* CHECK QUERY PARAMETERS
    Arguments - queryParams -       The query parameters that came in with the HTTP request
                requiredParams -    An array of strings, each representing a required query parameter
    return -    (throw error if there is a problem)
*/
function checkQueryParameters (queryParams, requiredParams) {

    // Check for required query parameters
    for (i = 0; i < requiredParams.length; i++) {
        if (!queryParams[requiredParams[i]] || queryParams[requiredParams[i]].length === 0) {
            throw new Error("missing parameter - " + requiredParams[i]);
        }
    }

    // Check for query parameter sanity
    if (queryParams.age !== undefined && queryParams.age.length > 0 && Number(queryParams.age) <= 0) {
        throw new Error("child's age must be greater than zero");
    }

}

/* GET OPTION OBJECTS
    Arguments - setupPrompt -   A string which is the pluralized form of a story set-up prompt name
                                For example "colors" or "locations"
                age -           The age of the child
    return -    options -       An array of JSON objects
                                each describes one of the options the user may choose
                                for the given story set-up prompt
                                each contains:
                                name -  The name of the option (can be used for onscreen display)
                                url OR hex (for images and colors respectively)
*/
async function getOptionObjects (setupPrompt, age) {
    var options = [];
    try {

        // Declare variables
        var randomColorName;
        var colorNameSet;
        var characterNames;
        var apiRequestURL;
        var urls;
        var urlIndex;
        var selectedURL;
        var optionNameSet;
        var optionName;
        var numOptions;
        var errorSuffix;
        var i;

        // How many options should we give the child for a given prompt?
        numOptions = ageToNumberOfOptions(age);

        // Create option objects (color)
        // Since colors come from randomly generated RGB values, could get duplicated colors
        // Keep trying until we get the correct number of unique colors
        // https://www.npmjs.com/package/color-convert
        if (setupPrompt === "colors") {
            colorNameSet = new Set([]);
            while (options.length < numOptions) {
                randomColorName =
                    convert.rgb.keyword(
                        randomNumber(255),
                        randomNumber(255),
                        randomNumber(255)
                    );
                if (colorNameSet.has(randomColorName) === false) {
                    colorNameSet.add(randomColorName);
                    options.push({
                        name: colorNameToOptionName(randomColorName),
                        value: "#" + convert.keyword.hex(randomColorName)
                    });
                }
            }
        }

        // Create option objects (names)
        // http://names.drycodes.com/
        // https://www.npmjs.com/package/axios
        else if (setupPrompt === "boys" || setupPrompt == "girls") {
            apiRequestURL =
                "http://names.drycodes.com/" +
                numOptions +
                "?nameOptions=" +
                setupPrompt.substring(0, setupPrompt.length - 1) +
                "_names";
            characterNames = (await axios.get(apiRequestURL)).data;
            options = characterNames.map(function(characterName){
                return {
                    // Most option types (color, animal, etc.) have a name and a value
                    // Character name IS the value conceptually
                    // Front end dev requests that we populate name and leave value blank
                    name: characterName.split("_")[0],
                    value: ""
                };
            });
        }

        // Create option objects (requiring file system access)
        else {
            // Get a COPY of the URLS of all image assets associated with the given setup prompt
            // We will be removing elements from the URL array to ensure we get unique options
            urls = JSON.parse(JSON.stringify(await getImageAssets(setupPrompt)));
            // Randomly pick unique options from available images, until we have enough
            // Guard against the possibility that we simply don't have enough images
            // (do not go into infinite loop in that case)
            optionNameSet = new Set([]);
            while (options.length < numOptions && urls.length > 0) {
                urlIndex = Math.floor(Math.random() * urls.length);
                selectedURL = urls.splice(urlIndex,1)[0];
                optionName = urlToOptionName(selectedURL);
                if (optionNameSet.has(optionName) === false) {
                    optionNameSet.add(optionName);
                    options.push({
                        name:   optionName,
                        value:  translateURL(selectedURL)
                    });
                }
            }
        }

        // Check what we came up with and complain if there's a problem
        errorSuffix = ", for setup prompt '" + setupPrompt + "' and age " + age;
        if (options.length === 0) {
            throw new Error("Unable to create any options" + errorSuffix);
        }
        for (i = 0; i < options.length; i++) {
            if (options[i].name === undefined || options[i].name.length === 0) {
                throw new Error("Created a bad option " + JSON.stringify(options[i]) + errorSuffix);
            }
        }

    }
    catch (err) {
        handleError(err);
    }
    return options;
}

/* COLOR NAME --> OPTION NAME
    Arguments - colorName -     A valid CSS color name
                                Example: "cornflowerblue"
    return -    optionName -    An option name for this color that is ready for display
                                Example: "Cornflower Blue"
*/
function colorNameToOptionName (colorName) {
    try {

        // Declare constants
        const BASIC_COLORS = [
            "black",
            "white",
            "gray",
            "red",
            "blue",
            "green",
            "yellow",
            "orange",
            "purple",
            "brown",
            "turquoise",
            "orchid",
            "pink",
            "khaki",
            "cyan",
            "magenta",
            "aquamarine",
            "violet",
            "coral",
            "almond",
            "goldenrod",
            "slate",
            "steel",
            "olive",
            "spring",
            "smoke",
            "drab"
        ];

        // Declare variables
        var optionName;
        var basicColorIndex;
        var i;

        // Split up the color name at every point where it includes basic color name
        optionName = colorName;
        for (i = 0;  i < BASIC_COLORS.length; i++) {
            basicColorIndex = optionName.indexOf(BASIC_COLORS[i]);
            if (basicColorIndex > 0) {
                optionName =
                    optionName.substr(0,basicColorIndex) +
                    " " +
                    optionName.substr(basicColorIndex,1).toUpperCase() +
                    optionName.substr(basicColorIndex+1);
            }
        }

        // Capitalize
        optionName = optionName.substr(0,1).toUpperCase() + optionName.substr(1);

        // Return
        return optionName;
    }
    catch (err) {
        handleError(err);
    }
}

/* TRANSLATE URL
    Arguments - urlIN -     The URL for an asset, relative to this file's location
    return -    urlOUT -    The URL for an asset served by express
                            - everything in src/assets/images is served
                            - this translated url is what the front end needs to access images
*/
function translateURL (urlIN) {
    return IMAGE_SERVE_URL + urlIN.split(IMAGES_DIRECTORY)[1];
}

/* GET IMAGE ASSETS
    Arguments - assetType -     The type of image asset desired
                                Possible values: "animals", "themes", "locations", etc.
                setupOption -   Optional argument
                                A selected option for the given asset type
                                For example "bunny" or "tiger" (if asset type is "animals")
    return -    assets -   Array of image URLS of the given asset type
*/
async function getImageAssets (assetType, setupOption) {
    try {

        // Declare variables
        var directory;
        var fileNames;
        var assets;

        // Singleton functionality
        if (imageAssets === undefined) {
            imageAssets = {};
        }
        if (imageAssets[assetType] === undefined) {
            directory = IMAGES_DIRECTORY + assetType;
            fileNames = await readdir(directory);
            if (fileNames === undefined) {
                throw new Error("Could not find any filenames at directory " + directory);
            }
            else {
                imageAssets[assetType] = fileNames.map(function(filename){
                    return directory + "/" + filename;
                });
            }
        }

        // Grab just those that the caller cares about (case and spacing insensitive)
        if (setupOption === undefined) {
            assets = imageAssets[assetType];
        }
        else {
            assets = imageAssets[assetType].filter(function(url){
                return url.toLowerCase().includes(setupOption.split(" ").join("_").toLowerCase());
            });
        }

        // Return
        return assets;

    }
    catch (err) {
        handleError(err);
    }
}

/* URL --> FILENAME
    Arguments - url -       A url
    return -    filName -   The filename (at the end of the URL)
*/
function urlToFileName (url) {
    try {
        var urlSplit = url.split("/");
        return urlSplit[urlSplit.length-1];
    }
    catch (err) {
        handleError(err);
    }
}

/* URL --> OPTION NAME
    Arguments - url -           A url
    return -    optionName -    An option name for this URL
                                "bedtime-story\src\assets\images\animals\bunny_2.jpg" --> "Bunny"
                                "bedtime-story\src\assets\images\locations\north_america.jpg" --> "South America"
*/
function urlToOptionName (url) {
    try {

        // Translate URL to option name
        optionName =
            // Get the filename
            urlToFileName(url)
            // Drop the file extension
            .split(".")[0]
            // Split by underscores
            .split("_")
            // Drop any part that is just a number (e.g. in "bunny_2", "2" isn't part of the option name)
            .filter(function(fileNamePart){
                return /[0-9]/.test(fileNamePart) === false;
            })
            // Capitalize the first letter of every word
            .map(function(fileNamePart){
                return fileNamePart.charAt(0).toUpperCase() + fileNamePart.slice(1);
            })
            // Join everything back up
            .join(" ");

        // Return
        return optionName;
    }
    catch (err) {
        handleError(err);
    }
}

/* AGE --> NUMBER OF OPTIONS
    Arguments - age -           The age of the child
    return -    numOptions -    The number of options to present to the child for any given prompt
*/
function ageToNumberOfOptions (age) {
    try {
        var numOptions = Math.ceil(age/2);
        numOptions = Math.min(6,numOptions);
        numOptions = Math.max(2,numOptions);
        return numOptions;
    }
    catch (err) {
        handleError(err);
    }
}

/* RANDOM NUMBER
    Arguments - max -       Maximum integer value
    return -    number -    Randomly selected number from 0 - max
*/
function randomNumber (max) {
    return Math.floor(Math.random() * max);
}

/* GET OPTION NAMES
    Arguments - setupPrompt -       A string which is the pluralized form of a story set-up prompt name
                                    For example "colors" or "locations"
                age -               The age of the child
                optionToInclude -   Optional argument
                                    One option to definitely include
                                    For example "red" or "africa"
    return -    options -           An array of strings
                                    each names one of the options the user may choose
                                    but does not fully specify it
                                    for example "colors" may return ["red", "orange", "blue"]
                                    but will not return the hex codes for these colors
                                    these simple return strings can be used in the tracery grammar
*/
async function getOptionNames (setupPrompt, age, optionToInclude) {

    var optionNames = [];
    try {

        // Declare variables
        var optionObjects;

        // Turn option objects into option names (for ease of use in tracery)
        optionObjects = await getOptionObjects(setupPrompt, age);
        optionNames = optionObjects.map(function(optionObject){
            return optionObject.name;
        });

        // Make sure to include the given option (if specified)
        if (optionToInclude !== undefined) {
            optionNames = optionNames.filter(function(optionName){
                return optionName !== optionToInclude;
            });
            optionNames.push(optionToInclude);
        }

    }
    catch (err) {
        handleError(err);
    }
    return optionNames;

}

/* GENERATE STORY CONTENT
    Arguments - setupOptions -  JSON object
                                each key is the name of a setup prompt
                                each value is the user-selected or randomly-selected
                                option that was chosen for that prompt
    return -    storyContent -  JSON object, containing:
                                text -      A string value containing the text
                                            of the generated children's story
                                images -    An array of URLs appropriate to display with the story
                                            For example if the child's favorite animal is "bunny",
                                                may contain several bunny images,
                                                as well as images of other animals
                                                randomly selected for inclusion in the story
                                            Similarly, may contain several images associated
                                                with the child's selected theme
                                            Etc.
*/
async function generateStoryContent (setupOptions) {
    var storyContent = {};
    try {

        // Declare variables
        var allStoryOptionNames;
        var allStoryImagePromises;
        var allStoryImageArrays;
        var storyBoys;
        var storyGirls;
        var storyAnimals;
        var storyColors;
        var storyTextLowerCase;
        var images;
        var i;

        // Generate story elements where we want not only the user's selection but also a few others
        // Only a few though!
        // Otherwise there's a chance that the child's preference won't make it into the story
        allStoryOptionNames = await Promise.all([
            getOptionNames("boys", 2, setupOptions.boy),
            getOptionNames("girls", 2, setupOptions.girl),
            getOptionNames("animals", 2, setupOptions.animal),
            getOptionNames("colors", 2, setupOptions.color)
        ]);
        storyBoys = allStoryOptionNames[0];
        storyGirls = allStoryOptionNames[1];
        storyAnimals = allStoryOptionNames[2];
        storyColors = allStoryOptionNames[3];

        // Generate the story text
        storyContent.text = await generateStoryText(
            setupOptions.age,
            setupOptions.location,
            setupOptions.theme,
            storyBoys,
            storyGirls,
            storyAnimals,
            storyColors
        ) + "  The End.";

        // Build up an array of promises for possibly time-consuming image search
        allStoryImagePromises = [
            getImageAssets("themes", setupOptions.theme),
            getImageAssets("locations", setupOptions.location)
        ];

        // Only look for animal images to include if the animal is actually present in the story
        // For example:
        //  in very short stories, we may have N available story animals,
        //  but only N-1 animals actually get mentioned in the story
        //  so we only want images for these N-1 animals to appear with the story
        storyTextLowerCase = storyContent.text.toLowerCase();
        for (i = 0; i < storyAnimals.length; i++) {
            if (storyTextLowerCase.includes(storyAnimals[i].toLowerCase())) {
                allStoryImagePromises.push(getImageAssets("animals", storyAnimals[i]));
            }
        }

        // Wait for all those images searches to conclude
        allStoryImageArrays = await Promise.all(allStoryImagePromises);

        // Flatten , translate (for front-end access), and sort (randomly) image URLS
        images = [];
        for (i = 0; i < allStoryImageArrays.length; i++) {
            images = images.concat(allStoryImageArrays[i]);
        }
        images = images
            .map(function(url){
                return translateURL(url);
            })
            .sort(function(a,b){
                return Math.random() >= 0.5;
            });
        storyContent.images = images;

    }
    catch (err) {
        handleError(err);
    }
    return storyContent;
}

/* GENERATE STORY TEXT
    Arguments - age -       The age of the child for whom the story is generated
                region -    The region of the world in which the story takes place
                            Possibilities are the words prior to a file extension in the filenames at:
                            bedtime-story\src\assets\images\locations
                theme -     The theme of the story
                            Options are the words prior to an underscore in the filenames at:
                            bedtime-story\src\assets\images\themes
                boys -      An array of boy's names that should be included in the story
                girls -     An array of girls' names that should be included in the story
                animals -   An array of animal names that should be included in the story
                            Possibilities are the words prior to an underscore in the filenames at:
                            bedtime-story\src\assets\images\animals
                colors -    An array of color names that could be included in the story
    return -    storyText - A string value containing the text
                            of the generated children's story
    notes -     Uses Tracery Grammar
                https://www.npmjs.com/package/tracery-grammar
*/
async function generateStoryText (age, region, theme, boys, girls, animals, colors) {
    var storyText = "";
    try {

        // Declare constants
        const MAX_NUM_SYNONYMS = 15;
        const MIN_AREA_COUNTRY = 100000;
        const MAX_AREA_COUNTRY = MIN_AREA_COUNTRY * 1000;

        // Declare variables
        var story1;
        var story2;
        var story3;
        var storyYoung1;
        var storyYoung2;
        var storyYoung3;
        var origin;
        var grammar;
        var storySet;
        var apiRequestURL;
        var apiPromises;
        var synonymSources;
        var synonymResponses;
        var synonyms;
        var inRegionCountries;
        var outOfRegionCountries;

        // Get synonyms (for varying vocabulary on each visit)
        // https://www.datamuse.com/api/
        // https://www.npmjs.com/package/axios
        synonyms = {};
        synonymSources = ["strange", "happy", "sad", "story", "small", "large", "town", "building", "fascinated", "beautiful", "wise", "puzzled", "nearby", "faraway", "tiring", "enjoyable"];
        apiPromises = [];
        for (i = 0; i < synonymSources.length; i++) {
            apiRequestURL = "https://api.datamuse.com/words?ml=" + synonymSources[i] + "&max=" + MAX_NUM_SYNONYMS;
            apiPromises.push(axios.get(apiRequestURL));
        }
        synonymResponses = await Promise.all(apiPromises);
        for (i = 0; i < synonymResponses.length; i++) {
            synonyms[synonymSources[i]] = synonymResponses[i].data.map(function(synResponse){
                return synResponse.word;
            });
        }

        // Get country names
        // https://fabian7593.github.io/CountryAPI/
        // https://www.npmjs.com/package/axios
        switch (region.toUpperCase()) {
            /* At the time this code was written, the supported regions in our app were:
                    Africa
                    Antarctica
                    Asia
                    Australia
                    Europe
                    North America
                    South America
                The way we use this API excludes some countries / sub-regions!
            */
            case "NORTH AMERICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=Northern%20America";
                break;
            case "SOUTH AMERICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=South%20America";
                break;
            case "AUSTRALIA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=Australia%20and%20New%20Zealand";
                break;
            case "ANTARCTICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pRegion=Polar";
                break;
            default:
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pRegion=" + region;
                break;
        }
        apiRequestURL += "&pAreaFrom=" + MIN_AREA_COUNTRY + "&pAreaTo=" + MAX_AREA_COUNTRY;
        inRegionCountries =
            (await axios.get(apiRequestURL)).data.Response
            .map(function(countryData){
                return countryData.Name.split("(")[0].trim();
            });
        apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries";
        apiRequestURL += "?pAreaFrom=" + MIN_AREA_COUNTRY + "&pAreaTo=" + MAX_AREA_COUNTRY;
        outOfRegionCountries =
            (await axios.get(apiRequestURL)).data.Response
            .map(function(countryData){
                return countryData.Name.split("(")[0].trim();
            })
            .filter(function(countryName){
                return inRegionCountries.includes(countryName) === false;
            });

        // Generate story 1
        story1 = "";
        story1 += "This is the #strange# #tale# of a #heroGender# #heroOccupation#. ";
        story1 += "Once upon a time, there was #heroOccupation.a# named #hero# who lived in #small.a# #town# in the country of #heroCountry#. ";
        story1 += "#they.capitalize# lived in a #small.a# house, along with a pet #color.a# #heroAnimal# named #petname#. ";
        story1 += "One day, #they# decided to explore the #locationCompare# #terrain# of #destination#. The #terrain# always #interested# #hero#, and #they# always wanted to travel with #petname# to #destination# and have fun. ";
        story1 += "#hero.capitalize# loved to travel by #transport#, and so looked for a #color# #transport# to go on a journey in. ";
        story1 += "On #their# way, #hero.capitalize# and #petname# saw many #beautiful# #animal.s# moving in #large# and #beautiful# #building.s# that stretched along the way for miles. ";
        story1 += "The journey to #destination# was #journeyDescription# and #journeyDescription#, but it was worth the visit. ";
        story1 += "When #they# got there, #hero.capitalize# met a #strange#, #wise# #occupation# named #heroFriend# who offered to show #them# around the #terrain# of #destination#. ";
        story1 += "#hero.capitalize#, #their# pet #heroAnimal# #petname#, and #their# new friend #heroFriend# explored the #terrain#, ate #food.s#, and played #game# for the rest of the day. #petname.capitalize# loved the snacks, and #hero# won lots of the games they played. ";
        story1 += "At the end of the day, #hero.capitalize# and #petname# were tired and decided to go back home. They lots of fun with #heroFriend#, and #they# promised to visit again, very soon. ";
        story1 += "#heroFriend.capitalize# gifted #hero# and #petname# with a #large# box of #fruit.s# to take back home to #heroCountry#. Though #hero# was #sad# #they# had to leave, the gift made #them# #happy# again. ";
        story1 += "#hero.capitalize# and #petname# said their goodbyes, and found a #large# #color# #transport# to go back to #heroCountry#. ";
        story1 += "When they finally reached home, #hero# and #petname# had a #small# snack of #bread# with #largeFruit#, and finally went to sleep.";

        // Generate story 2
        story2 = "";
        story2 += "This is the #strange# #tale# of a #heroGender# #heroOccupation#. ";
        story2 += "Once upon a time, there was #heroOccupation.a# named #hero# who lived in #small.a# #town# in the country of #heroCountry#. ";
        story2 += "#they.capitalize# lived in a #small.a# house, along with a pet #color.a# #heroAnimal# named #petname#. ";
        story2 += "#hero# was a #wise# #heroOccupation# who helped many people with their problems, and was known for solving many mysteries and puzzles. ";
        story2 += "One day, #their# friend #heroFriend# had a puzzle for #hero#. #heroFriend# said, \"I have a mystery for you to solve, my friend! Please help me!\". #hero# readily accepted the challenge! ";
        story2 += "#heroFriend# presented #hero# with #numberOfPeople# #puzOccupation.s#, who were all wearing identical #puzFeature.s# and said \"One of them ate my sandwich, and it was such a good sandwich too! ";
        story2 += "\"That sandwich was made with #bread# with #spice#-spiced #largeFruit# and #preparedMeat#, and I'd have eaten it too, but one of them ate it!\", #heroFriend# said. ";
        story2 += "\"Can you find out which of these #puzOccupation.s# ate my sandwich?\", asked #heroFriend#. ";
        story2 += "\"Sure I can!\" said #hero#. #they.capitalize# then walked around each of the #puzOccupation.s#, looking at them closely. ";
        story2 += "#hero# smiled, and then yelled out loud, \"The #puzOccupation# who ate your sandwich still has crumbs on their #puzFeature#!\" ";
        story2 += "One of the #puzOccupation.s# then jumped and tried to brush the crumbs off their #puzFeature#, but they saw that there were no crumbs to brush off. ";
        story2 += "\"A-ha! Found our mystery sandwich thief!\" said #hero#. \"I've solved your mystery, #heroFriend#!\" ";
        story2 += "#heroFriend# was #puzzled#. \"How did you know who the sandwich thief was?\", asked #heroFriend#. ";
        story2 += "\"I knew that the thief would would nervous about being found, so they'd try to hide the crumbs even if there were none!\" replied #hero#. ";
        story2 += "Everyone cheered #hero# for solving the mystery, and so they all went back to #hero#'s house to make more sandwiches.  ";
        story2 += "#hero# and #heroFriend# made more sandwiches made of #bread# with #spice#-spiced #largeFruit# and #fruit.s#, and then finally went to sleep. ";
        story2 += "That ends the tale of #hero# and the mystery of the sandwich thief.";

        // Generate story 3
        story3 = "";
        story3 += "This is the #strange# #tale# of a #heroGender# #heroOccupation#. ";
        story3 += "Once upon a time, there was #heroOccupation.a# named #hero# who lived in #small.a# #town# in the country of #heroCountry#. ";
        story3 += "#they.capitalize# lived in a #small.a# house, along with a pet #color.a# #heroAnimal# named #petname#. ";
        story3 += "#hero# was a #wise# #heroOccupation# who helped many people with their problems, and was known for solving many mysteries and puzzles. ";
        story3 += "One day, #their# friend #heroFriend# had a puzzle for #hero#. #heroFriend# said, \"I have a mystery for you to solve, my friend! Please help me!\". #hero# readily accepted the challenge! ";
        story3 += "#heroFriend# presented #hero# with #numberOfPeople# #puzOccupation.s#, who were all wearing identical #puzFeature.s# and said \"One of them stole my favorite #puzPossession#\". Can you help me find out which one of them #puzOccupation.s# is the thief?\" ";
        story3 += "#hero# looked at each one of the #puzOccupation.s#, and thought for a while. #they.capitalize# walked over to a pile of sticks, picked up a few of them, and returned to everyone else. ";
        story3 += "#hero# looked around and said,\" These are actually #strange#, magic sticks. Each of you #puzOccupation.s# must take one stick, keep it with you for a day and then return them to me tomorrow. You see, these magic sticks grow by two inches if they're held by a thief!\" ";
        story3 += "The next day, the #puzOccupation.s# returned their sticks to #hero#, who looked them over carefully. #they.capitalize# found that one of the sticks was two inches shorter than before! ";
        story3 += "#hero# pointed at the #puzOccupation# whose stick it was, and said \" This is the thief who took your #puzPossession#!\" ";
        story3 += "#heroFriend# was #puzzled#. \"How did you know who the #puzPossession# thief was?\", asked #heroFriend#. ";
        story3 += "\"I knew that the thief would would nervous about being found, so they'd try to cut their stick to hide the two extra inches! These are just normal sticks, they're not magic!\" replied #hero#. ";
        story3 += "Everyone cheered #hero# for solving the mystery, and so they all went back to #hero#'s house to make some sandwiches. ";
        story3 += "#hero# and #heroFriend# made some sandwiches made of #bread# with #spice#-spiced #largeFruit# and #fruit.s#, and then finally went to sleep. ";
        story3 += "That ends the tale of #hero# and the mystery of the #puzPossession# thief.";

        //Generate storyYoung1
        storyYoung1 = "";
        storyYoung1 += "Once upon a time, there was a #heroGender# #heroOccupation# named #hero# who lived in #small.a# #town# in the country of #heroCountry#. ";
        storyYoung1 += "#they.capitalize# lived in a #small.a# house, along with a pet #color.a# #heroAnimal# named #petname#. ";
        storyYoung1 += "One day, #they# decided to explore the #locationCompare# #terrain# of #destination#. ";
        storyYoung1 += "#hero.capitalize# looked for a #color# #transport# to go on a journey in. ";
        storyYoung1 += "On #their# way, #hero.capitalize# and #petname# saw many #beautiful# #animal.s# moving in #large# and #beautiful# #building.s#. ";
        storyYoung1 += "The journey to #destination# was #journeyDescription# and #journeyDescription#. ";
        storyYoung1 += "When #they# got there, #hero.capitalize# met a #wise# #occupation# named #heroFriend# who offered to show #them# around the #terrain# of #destination#. ";
        storyYoung1 += "#hero.capitalize#, #their# pet #heroAnimal# #petname#, and #their# new friend #heroFriend# explored the #terrain#, ate #food.s#, and played #game# for the rest of the day. ";
        storyYoung1 += "At the end of the day, #hero.capitalize# and #petname# were tired and decided to go back home. They lots of fun with #heroFriend#, and #they# promised to visit again soon. ";
        storyYoung1 += "#hero.capitalize# and #petname# said goodbye, and went back home.  ";

        //Generate storyYoung2
        storyYoung2 = "";
        storyYoung2 += "Once upon a time, there was a #heroGender# #heroOccupation# named #hero# who lived in #small.a# #town# in #heroCountry#. ";
        storyYoung2 += "#hero# was a #wise# #heroOccupation# who helped many people by solving mysteries and puzzles with #heroAnimal.a# as a partner in crime. ";
        storyYoung2 += "One day, #their# friend #heroFriend# said to #hero#, \"I have a mystery for you to solve, my friend! Please help me!\". #hero# said, \"What's the mystery?\" ";
        storyYoung2 += "#heroFriend# presented #hero# with #numberOfPeople# #puzOccupation.s#, who were all wearing identical #puzFeature.s# and said \"One of them ate my sandwich! ";
        storyYoung2 += "That sandwich was made with #bread# with #spice#-spiced #largeFruit# and #preparedMeat#! ";
        storyYoung2 += "Can you find out which of these #puzOccupation.s# ate my sandwich?\" ";
        storyYoung2 += "\"Sure I can!\" said #hero#. #they.capitalize# then walked around each of the #puzOccupation.s#, looking at them closely. ";
        storyYoung2 += "#hero# smiled, and then exclaimed, \"The #puzOccupation# who ate your sandwich still has crumbs on their #puzFeature#!\" ";
        storyYoung2 += "#petname.capitalize#, one of the #puzOccupation.s# then jumped and tried to brush the crumbs off their #puzFeature#. ";
        storyYoung2 += "\"A-ha! I found our mystery sandwich thief!\" said #hero#. ";
        storyYoung2 += "#heroFriend# was #puzzled#. \"How did you know who the sandwich thief was?\", asked #heroFriend#. ";
        storyYoung2 += "\"#petname.capitalize# still had crumbs on their #puzFeature# from eating the sandwich,\" replied #hero#. ";
        storyYoung2 += "Everyone cheered #hero# for solving the mystery, and they all went back to #hero#'s house to make more sandwiches.";

         //Generate storyYoung3
        storyYoung3 = "";
        storyYoung3 += "Once upon a time, there was a #heroGender# #heroAnimal# named #hero# from #heroCountry# who lived in #small.a# #town#. ";
        storyYoung3 += "#they.capitalize# lived in a #small.a# house, along with a pet #color.a# #heroAnimal# named #petname#. ";
        storyYoung3 += "It was morning, and #hero# wanted to play with #petname# the #animal#. ";
        storyYoung3 += "They played with #hero#'s #heroColor# #puzPossession.s# for many hours. Before they knew it, it was lunch time. ";
        storyYoung3 +=  "They left their #puzPossession.s# and went to eat #bread# and #fruit#. ";
        storyYoung3 += "But when they got back, their #puzPossession.s# were gone! ";
        storyYoung3 += "Off to the distance, #hero# saw #numberOfPeople# #puzOccupation.s# sneaking away to their #transport#. ";
        storyYoung3 += "#hero.capitalize# ran to them and said, \"One of you took our #puzPossession.s#! Who was it?\" ";
        storyYoung3 += "Each of the #puzOccupation.s# said, \"It wasn't me!\" But the last #puzOccupation# hesitated, and finally said, \"We all took your #puzPossession.s#.\" ";
        storyYoung3 += "#hero.capitalize#, looking #sad#, asked, \"Why did you take it from us?\" ";
        storyYoung3 += "The #puzOccupation.s# replied, \"We didn't have anything to play with.\" ";
        storyYoung3 += "#petname.capitalize# chimed in, \"Well, do you want to play with us?\ ";
        storyYoung3 += "The #puzOccupation.s# became #happy# and said, \"Yes!\" ";
        storyYoung3 += "#hero.capitalize# and #petname# not only got their #heroColor# #puzPossession.s# back but made new friends too!";
        
        // Generate origin
        origin = "";
        origin += "#";
        origin += "[hero:#heroName#][heroColor:#color#][heroAnimal:#animal#][heroOccupation:#occupation#][heroGender:#setGender#][petname:#petName#]";
        origin += "[heroFriend:#heroFriendName#][heroCountry:#inRegionCountry#][puzOccupation:#occupation#][puzFeature:#feature#][puzPossession:#possession#]";
        origin += "[destination:#outOfRegionCountry#][transport:#transport#]";
        origin += "story#";

        // Set of stories used is dependent upon age
        if (age <= 5) {
            storySet = [storyYoung1, storyYoung2, storyYoung3];
        } else {
            storySet = [story1, story2, story3];
        }

        // All together now
        grammar = tracery.createGrammar({
            // Use input from the user to influence the story
            // To prevent repeated name for heroName and petName, make "pool" of names for heroName unique from petName
            // Here, I allot 1/3 of the names to heroName, 1/3 to petName, 1/3 to heroFriendName
            heroName:           girls.slice(0, girls.length/3).concat(boys.slice(0, boys.length/3)),
            petName:            girls.slice(girls.length/3, girls.length * 2/3).concat(boys.slice(boys.length/3, boys.length * 2/3)),
            heroFriendName:     girls.slice(girls.length *2/3, girls.length).concat(boys.slice(boys.length *2/3, boys.length)),
            animal:             animals,
            color:              colors,
            numberOfPeople:     ["five", "ten", "six"],
            // Use synonyms from an API to keep things interesting
            strange:            filterWordsByAge(synonyms.strange, age),
            happy:              filterWordsByAge(synonyms.happy, age),
            sad:                filterWordsByAge(synonyms.sad, age),
            tale:               filterWordsByAge(synonyms.story, age),
            small:              filterWordsByAge(synonyms.small, age),
            large:              filterWordsByAge(synonyms.large, age),
            town:               filterWordsByAge(synonyms.town, age),
            building:           filterWordsByAge(synonyms.building, age),
            interested:         filterWordsByAge(synonyms.fascinated, age),
            beautiful:          filterWordsByAge(synonyms.beautiful, age),
            wise:               filterWordsByAge(synonyms.wise, age),
            puzzled:            filterWordsByAge(synonyms.puzzled, age),
            locationCompare:    filterWordsByAge(synonyms.nearby.concat(synonyms.faraway), age),
            journeyDescription: filterWordsByAge(synonyms.tiring.concat(synonyms.enjoyable), age),
            // Use other arrays of words to keep things interesting
            game:               filterWordsByAge(["hide and seek", "charades", "hopscotch", "tag", "dodgeball"], age),
            possession:         filterWordsByAge(["toy rabbit", "rubber ball", "teddy bear"]),
            food:               filterWordsByAge(["donut", "pizza", "hot dog", "ice cream cone"], age),
            setGender:          ["[they:they][them:them][their:their][theirs:theirs]", "[they:she][them:her][their:her][theirs:hers]", "[they:he][them:him][their:his][theirs:his]"],
            occupation:         filterWordsByAge(["wizard","witch","detective","ballerina","criminal","pirate","lumberjack","spy","doctor","scientist","captain","priest"], age),
            largeFruit :        filterWordsByAge(["kumquat", "honeydew", "bittermelon", "cherimoya", "peach", "sugar apple", "persimmon", "green apple", "jackfruit", "damson plum", "kiwi", "lime", "key lime", "meyer lemon", "pomegranate", "green apple", "pineapple", "mandarin orange", "blood orange", "plum", "bosque pear", "fig", "persimmon", "durian", "mango", "lychee"], age),
            preparedMeat :      filterWordsByAge(["duck fat", "roast duck", "crispy bacon", "pancetta", "salami", "prosciutto",  "pastrami", "roast game hen", "seared ahi"], age),
            herb :              filterWordsByAge(["fennel", "cilantro", "mint", "basil", "thyme", "Thai basil", "oregano", "peppermint", "spearmint", "rosemary"], age),
            spice :             filterWordsByAge(["vanilla", "nutmeg", "allspice", "turmeric", "cardamom", "saffron", "cinnamon", "chili powder", "cayenne", "coriander", "black pepper", "white pepper", "ginger", "za’atar"], age),
            terrain:            filterWordsByAge(["desert", "oasis", "rain forest", "jungle", "tundra", "mountains", "plains"], age),
            transport:          filterWordsByAge(["airplane", "ship", "boat", "car", "helicopter"], age),
            feature:            filterWordsByAge(["beard", "hat", "jacket", "robe", "mask"], age),
            fruit:              filterWordsByAge(["papaya", "apple", "strawberry", "banana", "orange", "mango"], age),
            bread:              filterWordsByAge(["rye bread", "biscuits", "sourdough bread", "pita bread", "naan"], age),
            // And finally some for which we do not want to filter by age
            inRegionCountry:    inRegionCountries,
            outOfRegionCountry: outOfRegionCountries,
            // Tracery requirements
            story:              storySet,
            origin:             [origin],
        });
        grammar.addModifiers(tracery.baseEngModifiers);
        storyText = grammar.flatten('#origin#');

    }
    catch (err) {
        handleError(err);
    }
    return storyText;
}

/* GET IN REGION COUNTRY URL
    Arguments - region -            The name of the child's preferred region
    return -    apiRequestURL -     The correct URL for the country name API
                                    to request a list of countries in this region
    // https://fabian7593.github.io/CountryAPI/
*/
function getInRegionCountryURL (region) {

    try {

        // Declare variables
        var apiRequestURL;

        // URL varies based on child's preferred region
        switch (region.toUpperCase()) {
            /* At the time this code was written, the supported regions in our app were:
                    Africa
                    Antarctica
                    Asia
                    Australia
                    Europe
                    North America
                    South America
                The way we use this API excludes some countries / sub-regions!
            */
            case "NORTH AMERICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=Northern%20America";
                break;
            case "SOUTH AMERICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=South%20America";
                break;
            case "AUSTRALIA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pSubRegion=Australia%20and%20New%20Zealand";
                break;
            case "ANTARCTICA":
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pRegion=Polar";
                break;
            default:
                apiRequestURL = "http://countryapi.gear.host/v1/Country/getCountries?pRegion=" + region;
                break;
        }
        apiRequestURL += "&pAreaFrom=" + MIN_AREA_COUNTRY + "&pAreaTo=" + MAX_AREA_COUNTRY;

        // Return
        return apiRequestURL;

    }
    catch (err) {
        handleError(err);
    }

}

/* FILTER SYNONYMS
    Arguments - synonymsIn -    An array of synonyms for some original word
                                Higher indices are less-good synonyms, generally speaking
                                The array may include duplicates
    return -    synonymsOut -   A filtered array of synonyms for some original words
                                Duplicates are removed
                                Synonyms are filtered for length according to child's age
                                Of the remaining synonyms, the first N are returned
                                Where N is defined by a constant within this method
*/
function filterSynonyms (synonymsIn, age) {

    // Declare constants
    const MAX_NUM_SYNONYMS = 3;

    // Declare variables
    var synonymsOut;

    // Remove duplicates
    synonymsOut = [...new Set(synonymsIn)];

    // Filter by age
    synonymsOut = filterWordsByAge(synonymsOut, age);

    // Return the correct number of synonyms
    synonymsOut = synonymsOut.slice(0,MAX_NUM_SYNONYMS);

    // Return
    return synonymsOut;

}

/* FILTER WORDS BY AGE
    Arguments - words -         An array of words that we may wish to include in our story
                age -           The child's age
    return -    filteredWords - A filtered array of words appropriate for the age
                                (based purely on word length)
*/
function filterWordsByAge (words, age) {

    // Declare variables
    var filteredWords;

    // Try to filter the words
    filteredWords = words.filter(function(word){
        return word.length <= Math.max(age*2.5);
    });

    // If all of the words were filtered out, forget about filtering
    if (filteredWords.length === 0) {
        filteredWords = words;
    }

    // Return
    return filteredWords;

}

/* HANDLE ERROR
    Arguments - err -           An error that has been thrown somewhere in the back end code
                response -      Handle to HTTP response (optional)
    return -    none
    notes -     If we have an HTTP response to put the error into,
                go ahead and respond with the error now.
                Otherwise, re-throw error and let someone else catch it,
                so it can bubble up to where it gets sent in a response
*/
function handleError (err, response) {
    if (response === undefined) {
        throw err;
    }
    else {
        console.error(err);
        return response.status(500).send({error: err.message});
    }
}
