const component = FullLegitCode.TcpSocket.Socket;

function openServer(successCallback, errorCallback, args) {
  try {
    const port = args[0];
    component.openServer(port)
    .then(
      server => {
        successCallback({event: 'open', id: server.id}, {keepCallback: true});
        server.addEventListener('clientopen', client => {
          successCallback({event: 'clientOpen', id: client.id, address: client.ip}, {keepCallback: true});
        });
      },
      e => errorCallback(e)
    );
  } catch (e) { errorCallback(e) }
}


module.exports = {
  openServer: openServer,

  // placeholders
  setDebug: function(successCallback) { successCallback() }
};

require('cordova/exec/proxy').add('FlcTcpSocket', module.exports);
