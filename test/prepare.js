const cmd = require('node-cmd');
const copyDirectory = require('./copy-directory');

copyDirectory('./android/app/src/main/java/pl/fulllegitcode/tcpsocket', '../src/android/pl/fulllegitcode/tcpsocket');
cmd.get(
  'cd cordova && cordova plugin remove cordova-plugin-flc-tcpsocket',
  (err, data, stderr) => {
    console.log(data);
    cmd.get(
      'cd cordova && cordova plugin add cordova-plugin-flc-tcpsocket --searchpath=../..',
      (err, data, stderr) => {
        if (err) {
          console.error(err);
          return;
        }
        if (stderr) {
          console.error(stderr);
          return;
        }
        console.log(data);
      }
    );
  }
);
