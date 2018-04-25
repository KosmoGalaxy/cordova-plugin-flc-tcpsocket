const component = FullLegitCode.TcpSocket.Socket;

let debug = false;

function clientListen(successCallback, errorCallback, args) {
  try {
    const id = args[0];
    component.clientListen(id).then(
      () => {},
      e => errorCallback(e),
      payload => {
        successCallback({data: payload.data.buffer, orderNo: payload.orderNo}, {keepCallback: true});
      }
    );
  } catch (e) { errorCallback(e) }
}

function closeClient(successCallback, errorCallback, args) {
  try {
    const id = args[0];
    component.closeClient(id).then(successCallback, errorCallback);
  } catch (e) { errorCallback(e) }
}

function closeServer(successCallback, errorCallback, args) {
  try {
    const id = args[0];
    component.closeServer(id).then(successCallback, errorCallback);
  } catch (e) { errorCallback(e) }
}

function openServer(successCallback, errorCallback, args) {
  try {
    const port = args[0];
    component.openServer(port)
    .then(
      server => {
        successCallback({event: 'open', id: server.id}, {keepCallback: true});
        server.addEventListener('clientopen', client => {
          successCallback({event: 'clientOpen', id: client.id, address: client.ip}, {keepCallback: true});
          client.addEventListener('closed', reason => {
            if (debug) {
              console.warn('[FlcTcpSocket.Client] closed (reason)=' + reason);
            }
            successCallback({event: 'clientClose', id: client.id, reason: reason}, {keepCallback: true});
          });
        });
        server.addEventListener('closed', () => successCallback({event: 'close'}));
      },
      e => successCallback({event: 'error', message: e.message})
    );
  } catch (e) { errorCallback(e) }
}

function setDebug(successCallback, errorCallback, args) {
  try {
    debug = args[0];
    successCallback();
  } catch (e) { errorCallback(e) }
}


module.exports = {
  clientListen: clientListen,
  closeClient: closeClient,
  closeServer: closeServer,
  openServer: openServer,
  setDebug: setDebug,

  // backwards compatibility alias
  clientReceive: clientListen
};

require('cordova/exec/proxy').add('FlcTcpSocket', module.exports);
