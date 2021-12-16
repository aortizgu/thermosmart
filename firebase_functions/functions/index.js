const functions = require('firebase-functions');
const admin = require('firebase-admin');

function checkActivation(app, activeRef, tempVal, thresholdVal) {
  return new Promise( resolve => {
    app.database().ref(activeRef).get().then(value => {
      const activeVal = value.val();
      console.log('checkActivation: thresholdVal ' + thresholdVal + ' tempVal ' + tempVal + ' active ' + activeVal)
      const needActivation = thresholdVal > tempVal;
      if (activeVal != needActivation) {
        if (activeVal && !needActivation) {
          console.log('checkActivation: needs deactivation')
        } else if (!activeVal && needActivation) {
          console.log('checkActivation: needs activation')
        }
        return app.database().ref(activeRef).set(needActivation).then(res => {
          console.log('checkActivation: done')
          return resolve();
        }).catch(err => {
          console.log('checkActivation: ' + err)
          return resolve().then(() => Promise.reject(err));
        });
      } else {
        console.log('checkActivation: no change needed')
        return resolve()
      }
    }).catch(err => {
      console.log('checkActivation: ' + err)
      return resolve().then(() => Promise.reject(err));
    });  
  });
}

exports.ondevicethreshold = functions.database.ref('/root/devices/{devId}/configuration/threshold')
  .onUpdate((snap, context) => {
    console.log('ondevicethreshold: change in device ', context.params.devId, ' threshold from ', snap.before.val(), 'to', snap.after.val());
    const thresholdVal = snap.after.val();
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, 'app');
    const deleteApp = () => app.delete().catch(() => null);
    const temperatureRef = snap.after.ref.parent.parent.child('status').child('temperature');
    const activeRef = snap.after.ref.parent.parent.child('status').child('active');
    return app.database().ref(temperatureRef).get().then(value => {
      const tempVal = value.val();
      console.log('ondevicethreshold: thresholdVal ' + thresholdVal + ' tempVal ' + tempVal)
      return checkActivation(app, activeRef, tempVal, thresholdVal).then( () => {
        console.log("ondevicethreshold: done");
        return deleteApp();
      });
    }).catch(err => {
      console.log('ondevicethreshold: ' + err)
      return deleteApp().then(() => Promise.reject(err));
    });

  });

exports.ondevicetemp = functions.database.ref('/root/devices/{devId}/status/temperature')
  .onUpdate((snap, context) => {
    console.log('ondevicetemp: change in device ', context.params.devId, ' temperature from ', snap.before.val(), 'to', snap.after.val());
    const tempVal = snap.after.val();
    const appOptions = JSON.parse(process.env.FIREBASE_CONFIG);
    appOptions.databaseAuthVariableOverride = context.auth;
    const app = admin.initializeApp(appOptions, 'app');
    const deleteApp = () => app.delete().catch(() => null);
    const thresholdRef = snap.after.ref.parent.parent.child('configuration').child('threshold');
    const activeRef = snap.after.ref.parent.child('active');
    return app.database().ref(thresholdRef).get().then(value => {
      const thresholdVal = value.val();
      console.log('ondevicetemp: thresholdVal ' + thresholdVal + ' tempVal ' + tempVal)
      return checkActivation(app, activeRef, tempVal, thresholdVal).then( () => {
        console.log("ondevicetemp: done");
        return deleteApp();
      });
    }).catch(err => {
      console.log('ondevicetemp: ' + err)
      return deleteApp().then(() => Promise.reject(err));
    });
  });

  exports.ondeviceactive = functions.database.ref('/root/devices/{devId}/status/active')
  .onUpdate((snap, context) => {
    functions.logger.info('change in device ', context.params.devId, ' active from ', snap.before.val(), ' to ', snap.after.val());
    //ToDo send notification to devices of device follower users
  });
