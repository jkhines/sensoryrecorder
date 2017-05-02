var config = {};

config.deviceOwner = "john.k.hines@asu.edu";
config.soundThreshhold = 30;
config.monitorSoundStartHour = 5;
config.monitorSoundEndHour = 22;

config.authServerUrl = process.env.AUTHPROTOCOL + "://" + process.env.AUTHSERVER + "/openid-connect-server-webapp";
config.apiServerUrl = process.env.APIPROTOCOL + "://" + process.env.APISERVER + "/rest-api";
config.deviceType = "sound";

module.exports = config;
