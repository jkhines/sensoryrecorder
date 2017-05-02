/**
 * @file
 * Description
 *
 * @author Author, Company
 *
 * @copyright (c) 2017, Holder
 */
/* jslint node:true */
/* jshint unused:true */

module.exports = function(config) {
    "use strict";

    var apiClient = {};

    // External Node.JS module for REST calls.
    try {
        require.resolve("request");
    }
    catch(error) {
        console.error("Critical Error [Missing Module]: 'request' Try 'npm install -g request' to fix.", error.toString());
        process.exit(-1);
    }
    apiClient.request = require("request");
    apiClient.Promise = require("promise");
    
    // Internal Node.JS module for File I/O.
    apiClient.fs = require("fs");
    
    // URL to the auth server.
    apiClient.authServerUrl = config.authServerUrl;
    // URL to the API server.
    apiClient.apiServerUrl = config.apiServerUrl;
    // The ID of this client on the auth server.
    apiClient.clientId = "";
    // The secret for this client on the auth server.
    apiClient.clientSecret = "";
    // Client access token.
    apiClient.token = "";
    // File in which client credentials are stored.
    apiClient.credentialsFile = '/node_app_slot/creds.json';
    // Device owner.
    apiClient.user = config.deviceOwner;
    // Device type.
    apiClient.type = config.deviceType;
    // Device name.
    apiClient.name = "iot-" + apiClient.type + " client for " + apiClient.user;

    apiClient.getCredentials = function() {
        return new apiClient.Promise(function (fulfill, reject) {
            // Create the credentials file if needed.
            apiClient.fs.exists(apiClient.credentialsFile, function (exists) {
                if (exists) {
                    fulfill();
                } else {
                    // Call the Auth server to dynamically register this device as a new client.
                    apiClient.request.post(apiClient.authServerUrl + "/register",
                        { json: { grant_types: "client_credentials", 
                                 client_name: apiClient.name,
                                 redirect_uris: [""] } },
                        function (error, response, body) {
                            if (!error) {
                                if (response.statusCode == 201) {
                                    console.log("Obtained device credentials");
                                    apiClient.clientId = body.client_id;
                                    apiClient.clientSecret = body.client_secret;

                                    // Write the credentials file for the next time we run.
                                    var fileContents = '{"client_id":"' + apiClient.clientId +
                                        '","client_secret":"' + apiClient.clientSecret + '"}';

                                    apiClient.fs.writeFile(apiClient.credentialsFile, fileContents, 'utf8', function (error) {
                                        if (!error) {
                                            fulfill();
                                        } else {
                                            console.error("Critical error [Unexpected error]: " + error.toString());
                                            reject();
                                        }
                                    });
                                } else {
                                    console.error("Critical Error [Unexpected response]: " + 
                                        response.statusCode + " " + response.statusMessage);
                                    reject();
                                }
                            } else {
                                console.error("Critical error [Unexpected error]: " + error.toString());
                                reject();
                            }
                        });
                }
            });
        });
    };
    
    apiClient.getToken = function() {
        return new apiClient.Promise(function (fulfill, reject) {
            // Read the credentials file and get a token.
            apiClient.fs.readFile(apiClient.credentialsFile, function (error, data) {
                if (!error) {
                    var jsonObject = JSON.parse(data.toString());
                    apiClient.clientId = jsonObject.client_id;
                    apiClient.clientSecret = jsonObject.client_secret;

                    // POST the credentials to the token endpoint to get a token.
                    apiClient.request.post(apiClient.authServerUrl + "/token",
                        { form: { client_id: apiClient.clientId,
                                 client_secret: apiClient.clientSecret,
                                 grant_type: "client_credentials" } },
                        function (error, response, body) {
                            if (!error) {
                                if (response.statusCode == 200) {
                                    var jsonObject = JSON.parse(body);
                                    apiClient.token = jsonObject.access_token;
                                    console.log("Obtained access token");
                                    fulfill();
                                } else {
                                    console.error("Critical Error [Unexpected response]: " + 
                                        response.statusCode + " " + response.statusMessage);
                                    reject();
                                }
                            } else {
                                console.error("Critical error [Unexpected error]: " + error.toString());
                                reject();
                            }
                        });
                } else {
                    console.error("Critical error [Unexpected error]: " + error.toString());
                    reject();
                }
            });
        });
    };

    apiClient.registerDevice = function() { 
        return new apiClient.Promise(function (fulfill, reject) {
            // Submit a device registration request to the owner.
            apiClient.request.post(apiClient.apiServerUrl + "/devices", 
                { json: { client_id: apiClient.clientId, 
                         client_name: apiClient.name,
                         username: apiClient.user,
                         approved: false } },
                function (error, response) {
                    if (!error) {
                        if (response.statusCode == 200) {
                            console.log("Registration request sent");
                            fulfill();
                        } else {
                            console.error("Critical Error [Unexpected response]: " + 
                                response.statusCode + " " + response.statusMessage);
                            reject();
                        }
                    } else {
                        console.error("Critical error [Unexpected error]: " + error.toString());
                        reject();
                    }
                }).auth(null, null, true, apiClient.token);
        });
    };

    apiClient.saveData = function(data) {
        return new apiClient.Promise(function (fulfill, reject) {
            // Submit a data point.
            apiClient.request.post(apiClient.apiServerUrl + "/data", 
                { json: { username: apiClient.user,
                         type: data.type, 
                         quantity: data.quantity } },
                function (error, response) {
                    if (!error) {
                        if (response.statusCode == 200) {
                            console.log("Data point saved");
                            fulfill();
                        } else {
                            console.error("Critical Error [Unexpected response]: " + 
                                response.statusCode + " " + response.statusMessage + " " + response.body);
                            reject();
                        }
                    } else {
                        console.error("Critical error [Unexpected error]: " + error.toString());
                        reject();
                    }
                }).auth(null, null, true, apiClient.token);
        });
    };
    
    return apiClient;
};
