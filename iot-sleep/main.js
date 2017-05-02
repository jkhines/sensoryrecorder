/* jslint node:true */
/* jshint unused:true *//////////////////////
"use strict";
var config = require("./config");
var Promise = require("promise");

console.log("Initializing DSSR IoT Sleep Client");
var apiClient = require("./lib/api_client.js")(config);

var recordMotion = function() {
    return new Promise(function (fulfill, reject) {
        console.log("Beginning motion detection");
        // Load Grove Motion module.
        var grove_motion = require('jsupm_biss0001');

        // Instantiate a Grove Motion sensor on GPIO pin D2.
        var myMotionObj = new grove_motion.BISS0001(2);
        var inTimeWindow = false;
        var alreadySaved = false;

        // Detect motion every quarter second.
        setInterval(function () {
            var hour = (new Date()).getHours();

            // Check if we are entering a monitoring time window for the first time.
            if (((hour >= config.monitorWakeStartHour && hour < config.monitorWakeEndHour) || 
                 (hour >= config.monitorSleepStartHour && hour < config.monitorSleepEndHour)) && 
                !inTimeWindow) {
                console.log("Entering monitoring time window [" + (new Date()).toLocaleTimeString() + "]");
                alreadySaved = false;
            }

            // Set a flag indicating if we are within a monitoring time window.
            inTimeWindow = ((hour >= config.monitorWakeStartHour && hour < config.monitorWakeEndHour) || 
                            (hour >= config.monitorSleepStartHour && hour < config.monitorSleepEndHour));

            // If a motion event occurs and has not yet been saved, save it.
            if (inTimeWindow && !alreadySaved && myMotionObj.value()) {
                var event = "";
                if ((hour >= config.monitorWakeStartHour && hour < config.monitorWakeEndHour)) {
                    event = "wake";
                } else if (hour >= config.monitorSleepStartHour && hour < config.monitorSleepEndHour) {
                    event = "sleep";
                }

                console.log("Detected motion at " + (new Date()).toLocaleTimeString() + 
                            " - submitting " + event + " event");

                // Use the client ID and secret like a refresh token and always grab a new token.
                apiClient.getCredentials()
                    .then(apiClient.getToken)
                    .then(function() { return {type: event, quantity: null}; })
                    .done(apiClient.saveData);

                alreadySaved = true;
            }
        }, 250);
        fulfill();

        // Print message when exiting
        process.on('SIGINT', function () {
            console.log("Exiting...");
            reject();
        });
    });
};

// Initialize the API client to ensure this device
// is registered and associated with a user, then
// start detecting motion.
apiClient.getCredentials()
    .then(apiClient.getToken)
    .then(apiClient.registerDevice)
    .done(recordMotion);
