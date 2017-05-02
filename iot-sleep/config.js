var config = {};

config.deviceOwner = "john.k.hines@asu.edu";
config.monitorWakeStartHour = 4;   // 4 A.M. local time
config.monitorWakeEndHour = 8;     // 8 A.M. local time
config.monitorSleepStartHour = 19; // 7 P.M. local time
config.monitorSleepEndHour = 22;   // 10 P.M. local time

config.authServerUrl = process.env.AUTHPROTOCOL + "://" + process.env.AUTHSERVER + "/openid-connect-server-webapp";
config.apiServerUrl = process.env.APIPROTOCOL + "://" + process.env.APISERVER + "/rest-api";
config.deviceType = "sleep";

module.exports = config;
