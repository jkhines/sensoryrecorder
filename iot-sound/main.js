/* jslint node:true */
/* jshint unused:true */
"use strict";
var config = require('./config');
var Promise = require('promise');

console.log("Initializing DSSR IoT Sound Client");
var apiClient = require("./lib/api_client.js")(config);

var recordSound = function() {
    return new Promise(function (fulfill, reject) {
        console.log("Beginning sound detection");

        // Sample code based on:
        // https://software.intel.com/en-us/iot/hardware/sensors/analog-microphone
        var upmMicrophone = require("jsupm_mic");
        // Attach microphone to analog port A0
        var myMic = new upmMicrophone.Microphone(0);
        var inTimeWindow = false;
        
        var threshContext = new upmMicrophone.thresholdContext();
        threshContext.averageReading = 0;
        threshContext.runningAverage = 0;
        threshContext.averagedOver = 2;
        
        // Sample the ambient noise approximately every five minutes.
        // Repeatedly, take a sample every 140 microseconds;
        // find the average of 1024 samples; and
        // print a running graph of the averages
        // It is the sound sampling that takes five minutes, not the setInterval.
        setInterval(function () {
            var hour = (new Date()).getHours();

            // Check if we are entering a monitoring time window for the first time.
            if (hour >= config.monitorSoundStartHour && hour < config.monitorSoundEndHour && !inTimeWindow) {
                console.log("Entering monitoring time window [" + (new Date()).toLocaleTimeString() + "]");
            }

            // Set a flag indicating if we are within a monitoring time window.
            inTimeWindow = (hour >= config.monitorSoundStartHour && hour < config.monitorSoundEndHour);

            if (inTimeWindow) {
                console.log("Measuring sound threshold at " + (new Date()).toLocaleTimeString());
                var buffer = new upmMicrophone.uint16Array(1024);
                var len = myMic.getSampledWindow(140, 1024, buffer);
                if (len) {
                    var thresh = myMic.findThreshold(threshContext, 30, buffer, len);
                    if (thresh && thresh > config.soundThreshhold) {
                        console.log("Detected sound " + thresh + " greater than threshold " + 
                                    config.soundThreshhold + " at " + (new Date()).toLocaleTimeString() + 
                                    " - submitting event");

                        // Use the client ID and secret like a refresh token and always grab a new token.
                        apiClient.getCredentials()
                            .then(apiClient.getToken)
                            .then(function() { return {type: "sound", quantity: thresh}; })
                            .done(apiClient.saveData);
                    }
                }
            }
        }, 1000);
        fulfill();
	 
        // Print message when exiting
        process.on('SIGINT', function() {
            console.log("Exiting...");
            reject();
        });
    });
};

apiClient.getCredentials()
    .then(apiClient.getToken)
    .then(apiClient.registerDevice)
    .done(recordSound);
