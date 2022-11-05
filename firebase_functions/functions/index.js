/* eslint-disable require-jsdoc */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const logger = require("firebase-functions/logger");

async function getVal(app, ref) {

  var val = undefined;

  await app.database().ref(ref).get().then((snapshot) => {
    val = snapshot.val();
  }).catch((err) => {
    logger.error("getVal: error", err);
  });

  return val;

}

async function sendNotification(app, payload, user) {

  logger.info("sendNotification: payload", JSON.stringify(payload), ", user", user);

  const tokenRef = "/root/users/" + user + "/token";
  const token = await getVal(app, tokenRef);
  if (token == undefined || !token) {
    logger.error("sendNotification: invalid token", token);
    return;
  }

  var tokenError = undefined;
  await app.messaging().sendToDevice(token, payload)
    .then((response) => {
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          logger.error("sendNotification: Failure sending notification to", token, error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === "messaging/invalid-registration-token" ||
            error.code === "messaging/registration-token-not-registered") {
            tokenError = true;
          }
        }
      });
    }).catch((err) => {
      logger.error("sendNotification: error when sending notification to ", token, " error ", err);
    });

  if (tokenError != undefined && tokenError) {
    logger.error("sendNotification: invalid token", token);
    await app.database().ref(tokenRef).remove().then(() => {
      logger.info("sendNotification: deleted token", token);
    }).catch((err) => {
      logger.error("sendNotification: error deleting token", err);
    });
  }

}

async function onActivationChange(app, payload, devId) {

  const config = await getVal(app, "/root/devices/" + devId + "/configuration");
  if (config == undefined) {
    logger.error("onActivationChange: invalid config", config);
    return;
  }

  payload.name = config.name;

  for (let index = 0; index < config.followers.length; index++) {
    const folower = config.followers[index]
    await sendNotification(app, payload, folower);
  }

}

async function checkBoilerActivation(app, devId, tempVal, boilerConfig) {

  const activeRef = "/root/devices/" + devId + "/status/outputs/boiler";
  const activeVal = await getVal(app, activeRef);
  if (activeVal == undefined) {
    logger.error("checkBoilerActivation: invalid activeVal", activeVal);
    return;
  }

  let needActivation = false;
  if (boilerConfig.automaticActivationEnabled) {
    const hysteresis = 0.50;
    if (activeVal) {
      needActivation = tempVal < (boilerConfig.threshold + hysteresis);
    } else {
      needActivation = tempVal < (boilerConfig.threshold - hysteresis);
    }  
  }

  logger.debug("checkBoilerActivation: threshold", boilerConfig.threshold
    , ", tempVal", tempVal
    , ", active", activeVal
    , ", needActivation", needActivation
    , ", automaticActivationEnabled", boilerConfig.automaticActivationEnabled);

  if (activeVal != needActivation) {
    if (activeVal && !needActivation) {
      logger.info("checkBoilerActivation: needs deactivation");
    } else if (!activeVal && needActivation) {
      logger.info("checkBoilerActivation: needs activation");
    }
    await app.database().ref(activeRef).set(needActivation).then(() => {
      logger.info("checkWateringActivation: done");
    }).catch((err) => {
      logger.error("checkWateringActivation: error", err);
    });
  } else {
    logger.info("checkBoilerActivation: no change needed");
  }

}

async function checkAutomatedActivationNeeded(app, devId) {

  const wateringConfigRef = "/root/devices/" + devId + "/configuration/watering";
  const wateringConfig = await getVal(app, wateringConfigRef);
  if (wateringConfig == undefined) {
    logger.error("checkAutomatedActivationNeeded: invalid wateringConfig", wateringConfig);
    return false;
  }

  if (wateringConfig.automaticActivationEnabled == false) {
    logger.debug("checkAutomatedActivationNeeded: automated activation disabled");
    return false;
  }

  const nextWateringActivationRef = "/root/devices/" + devId + "/status/nextWateringActivation";
  const nextWateringActivation = await getVal(app, nextWateringActivationRef);
  if (nextWateringActivation == undefined || isNaN(nextWateringActivation)) {
    logger.error("checkAutomatedActivationNeeded: invalid nextWateringActivation", nextWateringActivation);
    return false;
  }

  const epochNow = Math.round(Date.now() / 1000);
  if (epochNow < nextWateringActivation) {
    logger.debug("checkAutomatedActivationNeeded: automated activation does not need to be activated");
    return false;
  }

  let nextDate = new Date();
  nextDate.setDate(nextDate.getDate() + wateringConfig.frequencyDay);
  nextDate.setHours(wateringConfig.activationHour);
  nextDate.setMinutes(0);
  nextDate.setSeconds(0);
  nextDate.setMilliseconds(0);
  const nextEpochTime = Math.round(nextDate.getTime() / 1000);
  logger.info("onwateringdeviceactive: updating next activation time, ", nextEpochTime);

  await app.database().ref(nextWateringActivationRef).set(nextEpochTime).then(() => {
    logger.info("onwateringdeviceactive: updated next activation time, ", nextEpochTime);
  }).catch((err) => {
    logger.error("onwateringdeviceactive: error", err);
  });

  return true;

}

async function checkWateringActivation(app, devId, lastWateringActivation) {

  const activeRef = "/root/devices/" + devId + "/status/outputs/watering";
  const activeVal = await getVal(app, activeRef);
  if (activeVal == undefined) {
    logger.error("checkWateringActivation: invalid activeVal", activeVal);
    return;
  }

  const epochNow = Math.round(Date.now() / 1000)

  const wateringConfigRef = "/root/devices/" + devId + "/configuration/watering";
  const wateringConfig = await getVal(app, wateringConfigRef);
  if (wateringConfig == undefined) {
    logger.error("checkAutomatedActivationNeeded: invalid wateringConfig", wateringConfig);
    return;
  }

  const automatedActivationNeeded = await checkAutomatedActivationNeeded(app, devId);
  if (automatedActivationNeeded) {
    logger.debug("checkWateringActivation: update lastWateringActivation to current epoch in order to trigger the activation");

    const lastWateringActivationRef = "/root/devices/" + devId + "/status/lastWateringActivation";
    await app.database().ref(lastWateringActivationRef).set(epochNow).then(() => {
      logger.info("checkWateringActivation: updated lastWateringActivation", epochNow);
    }).catch((err) => {
      logger.error("checkWateringActivation: error setting lastWateringActivation", err);
    });

  } else {
    const durationSeconds = wateringConfig.durationMinute * 60;
    const timeSinceLastActivation = epochNow - lastWateringActivation;
    const needActivation = timeSinceLastActivation < durationSeconds;

    logger.debug("checkWateringActivation: needActivation", needActivation
      , ", activeVal", activeVal
      , ", epochNow", epochNow
      , ", timeSinceLastActivation", timeSinceLastActivation
      , ", durationSeconds", durationSeconds);

    if (activeVal != needActivation) {
      if (activeVal && !needActivation) {
        logger.info("checkWateringActivation: needs deactivation");
      } else if (!activeVal && needActivation) {
        logger.info("checkWateringActivation: needs activation");
      }
      await app.database().ref(activeRef).set(needActivation).then(() => {
        logger.info("checkWateringActivation: done");
      }).catch((err) => {
        logger.error("checkWateringActivation: error", err);
      });
    } else {
      logger.info("checkWateringActivation: no change needed");
    }
  }
}

exports.onboilerconfig = functions.database.ref("/root/devices/{devId}/configuration/boiler")
  .onUpdate(async (snap, context) => {
    logger.info("onboilerconfig: change in device", context.params.devId, "boiler config from", JSON.stringify(snap.before.val()), "to", JSON.stringify(snap.after.val()));
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    const tempVal = await getVal(app,  "/root/devices/" + context.params.devId + "/status/temperature");
    if (tempVal == undefined || isNaN(tempVal)) {
      logger.error("onboilerconfig: invalid tempVal", tempVal);
      return deleteApp();
    }

    await checkBoilerActivation(app, context.params.devId, tempVal, snap.after.val());
    return deleteApp();
  });

exports.onboilertemperature = functions.database.ref("/root/devices/{devId}/status/temperature")
  .onUpdate(async (snap, context) => {
    logger.info("onboilertemperature: change in device", context.params.devId, "temperature from", snap.before.val(), "to", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    const boilerConfig = await getVal(app,  "/root/devices/" + context.params.devId + "/configuration/boiler");
    if (boilerConfig == undefined) {
      logger.error("onboilertemperature: invalid boilerConfig", boilerConfig);
      return deleteApp();
    }

    await checkBoilerActivation(app, context.params.devId, snap.after.val(), boilerConfig);
    return deleteApp();
  });

exports.onlastwateringactivation = functions.database.ref("/root/devices/{devId}/status/lastWateringActivation")
  .onUpdate(async (snap, context) => {
    logger.info("onlastwateringactivation: change in device", context.params.devId, "lastWateringActivation from", snap.before.val(), "to", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    if (isNaN(snap.after.val()) || snap.after.val() <= 0) {
      logger.error("onlastwateringactivation: invalid lastWateringActivation", lastWateringActivation);
      return deleteApp();
    }

    await checkWateringActivation(app, context.params.devId, snap.after.val());
    return deleteApp();
  });

exports.onheartbeat = functions.database.ref("/root/devices/{devId}/status/esp8266/heartbeat")
  .onUpdate(async (snap, context) => {
    logger.info("onHeartBeat: change in device", context.params.devId, "heartbeat from", snap.before.val(), "to", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    const lastWateringActivationRef = "/root/devices/" + context.params.devId + "/status/lastWateringActivation";
    const lastWateringActivation = await getVal(app, lastWateringActivationRef);
    if (isNaN(lastWateringActivation) || lastWateringActivation <= 0) {
      logger.error("onHeartBeat: invalid lastWateringActivation", lastWateringActivation);
      return deleteApp();
    }

    await checkWateringActivation(app, context.params.devId, lastWateringActivation);
    return deleteApp();
  });

exports.onwateringdeviceactive = functions.database.ref("/root/devices/{devId}/status/outputs/watering")
  .onUpdate(async (snap, context) => {
    logger.info("onwateringdeviceactive: change in device", context.params.devId, "output watering from", snap.before.val(), "to", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    const payload = {
      data: {
        id: context.params.devId,
        state: snap.after.val().toString(),
        system: "watering"
      },
    };

    await onActivationChange(app, payload, context.params.devId);
    return deleteApp();
  });

exports.onboilerdeviceactive = functions.database.ref("/root/devices/{devId}/status/outputs/boiler")
  .onUpdate(async (snap, context) => {
    logger.info("onboilerdeviceactive: change in device", context.params.devId, "output boiler from", snap.before.val(), "to", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);

    const payload = {
      data: {
        id: context.params.devId,
        state: snap.after.val().toString(),
        system: "boiler"
      },
    };

    await onActivationChange(app, payload, context.params.devId);
    return deleteApp();
  });
