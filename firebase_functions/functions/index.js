/* eslint-disable require-jsdoc */
const functions = require("firebase-functions");
const admin = require("firebase-admin");

function checkActivation(app, activeRef, tempVal, thresholdVal) {
  return new Promise((resolve) => {
    app.database().ref(activeRef).get().then((value) => {
      const activeVal = value.val();
      console.log("checkActivation: thresholdVal " + thresholdVal + " tempVal " + tempVal + " active " + activeVal);
      const needActivation = thresholdVal > tempVal;
      if (activeVal != needActivation) {
        if (activeVal && !needActivation) {
          console.log("checkActivation: needs deactivation");
        } else if (!activeVal && needActivation) {
          console.log("checkActivation: needs activation");
        }
        return app.database().ref(activeRef).set(needActivation).then((res) => {
          console.log("checkActivation: done");
          return resolve();
        }).catch((err) => {
          console.log("checkActivation: " + err);
          return resolve().then(() => Promise.reject(err));
        });
      } else {
        console.log("checkActivation: no change needed");
        return resolve();
      }
    }).catch((err) => {
      console.log("checkActivation: " + err);
      return resolve().then(() => Promise.reject(err));
    });
  });
}

exports.ondevicethreshold = functions.database.ref("/root/devices/{devId}/configuration/threshold")
  .onUpdate((snap, context) => {
    console.log("ondevicethreshold: change in device ", context.params.devId, " threshold from ", snap.before.val(), "to", snap.after.val());
    const thresholdVal = snap.after.val();
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);
    const temperatureRef = snap.after.ref.parent.parent.child("status").child("temperature");
    const activeRef = snap.after.ref.parent.parent.child("status").child("active");
    return app.database().ref(temperatureRef).get().then((value) => {
      const tempVal = value.val();
      console.log("ondevicethreshold: thresholdVal " + thresholdVal + " tempVal " + tempVal);
      return checkActivation(app, activeRef, tempVal, thresholdVal).then(() => {
        console.log("ondevicethreshold: done");
        return deleteApp();
      });
    }).catch((err) => {
      console.log("ondevicethreshold: " + err);
      return deleteApp().then(() => Promise.reject(err));
    });
  });

exports.ondevicetemp = functions.database.ref("/root/devices/{devId}/status/temperature")
  .onUpdate((snap, context) => {
    console.log("ondevicetemp: change in device ", context.params.devId, " temperature from ", snap.before.val(), "to", snap.after.val());
    const tempVal = snap.after.val();
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);
    const thresholdRef = snap.after.ref.parent.parent.child("configuration").child("threshold");
    const activeRef = snap.after.ref.parent.child("active");
    return app.database().ref(thresholdRef).get().then((value) => {
      const thresholdVal = value.val();
      console.log("ondevicetemp: thresholdVal " + thresholdVal + " tempVal " + tempVal);
      return checkActivation(app, activeRef, tempVal, thresholdVal).then(() => {
        console.log("ondevicetemp: done");
        return deleteApp();
      });
    }).catch((err) => {
      console.log("ondevicetemp: " + err);
      return deleteApp().then(() => Promise.reject(err));
    });
  });

exports.ondeviceactive = functions.database.ref("/root/devices/{devId}/status/active")
  .onUpdate((snap, context) => {
    console.log("change in device ", context.params.devId, " active from ", snap.before.val(), " to ", snap.after.val());
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, "app");
    const deleteApp = () => app.delete().catch(() => null);
    const deviceNameRef = snap.after.ref.parent.parent.child("configuration").child("name");
    return app.database().ref(deviceNameRef).get().then((deviceNameSnapshot) => {
      const deviceNameVal = deviceNameSnapshot.val();
      const message = "Thermostat " + deviceNameVal + " has been " + (snap.after.val() ? "activated" : "deactivated");
      const payload = {
        data: {
          title: snap.after.val() ? "Activated" : "Deactivated",
          body: message,
          thermostat: context.params.devId,
        },
      };
      const folowersRef = snap.after.ref.parent.parent.child("configuration").child("followers");
      return app.database().ref(folowersRef).get().then((folowersSnapshot) => {
        console.log("ondeviceactive: iterate over device followers");
        const folowersPromises = [];
        folowersSnapshot.forEach((folowerSnapshot) => {
          const followerVal = folowerSnapshot.val();
          folowersPromises.push(new Promise((resolve) => {
            console.log("ondeviceactive: iterate over follower " + followerVal + " devices");
            app.database().ref("/root/users/" + followerVal + "/token").get().then((tokensSnapshot) => {
              const token = tokensSnapshot.val();
              if (token) {
                console.log("ondeviceactive: sending notification to user " + followerVal + ", device token " + token);
                app.messaging().sendToDevice(token, payload).then((response) => {
                  const tokensToRemove = [];
                  response.results.forEach((result, index) => {
                    const error = result.error;
                    if (error) {
                      console.log("Failure sending notification to", token, error);
                      // Cleanup the tokens who are not registered anymore.
                      if (error.code === "messaging/invalid-registration-token" ||
                        error.code === "messaging/registration-token-not-registered") {
                        tokensToRemove.push(tokensSnapshot.ref.child(token).remove());
                      }
                    }
                  });
                  return Promise.all(tokensToRemove).then(() => {
                    console.log("ondeviceactive: exit from tokens to remove " + tokensSnapshot.ref);
                  }).catch((err) => {
                    console.log("ondeviceactive: error when exit from tokens to remove " + tokensSnapshot.ref + " error " + err);
                  });
                }).then(() => {
                  console.log("ondeviceactive: exit from " + tokensSnapshot.ref);
                  return resolve();
                }).catch((err) => {
                  console.log("ondeviceactive: error when exit from " + tokensSnapshot.ref + " error " + err);
                  return resolve();
                });
              } else {
                console.log("ondeviceactive: invalid token from " + followerVal);
                return resolve();
              }
            }).catch((err) => {
              console.log("ondeviceactive: error " + err);
              return resolve();
            });
          }));
        });
        return Promise.all(folowersPromises).then(() => {
          console.log("ondeviceactive: exit from folowersPromises");
        }).catch((err) => {
          console.log("ondeviceactive: error when exit from folowersPromises error " + err);
        });
      }).then(() => {
        console.log("ondeviceactive: exit from " + folowersRef);
      }).catch((err) => {
        console.log("ondeviceactive: error" + err);
      });
    }).then(() => {
      console.log("ondeviceactive: exit from " + deviceNameRef);
      return deleteApp();
    }).catch((err) => {
      console.log("ondeviceactive: error" + err);
      return deleteApp().then(() => Promise.reject(err));
    });
  });
