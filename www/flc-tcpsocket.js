const exec = require('cordova/exec');

function FlcTcpSocket() {}

FlcTcpSocket.setDebug = function (value) {
  exec(
    function () {},
    function () {},
    'FlcTcpSocket',
    'setDebug',
    [value]
  );
};

FlcTcpSocket.openServer = function (port, successCallback, errorCallback) {
  let server;
  exec(
    function (payload) {
      if (payload.event === 'open') {
        server = new FlcTcpServer(payload.id, port);
        if (successCallback) {
          successCallback(server);
        }
      } else if (payload.event === 'client' || payload.event === 'clientOpen') {
        const client = new FlcTcpClient(payload.id, payload.address);
        server._addClient(client);
        if (server.onClient) {
          server.onClient(client);
        }
      } else if (payload.event === 'clientClose') {
        server._closeClient(payload.id);
      } else if (payload.event === 'close') {
        server._close();
      } else if (payload.event === 'error') {
        if (server.onError) {
          server.onError(payload.message);
        }
      }
    },
    function (message) {
      if (errorCallback) {
        errorCallback(message);
      }
    },
    'FlcTcpSocket',
    'openServer',
    [port]
  );
};

function FlcTcpServer(id, port) {
  this.id = id;
  this.port = port;
  this.clients = [];
  this.isClosed = false;
  this.onClient = null;
  this.onClose = null;
  this.onError = null;
}

FlcTcpServer.prototype.close = function() {
  if (!this.isClosed) {
    exec(
      function() {},
      function() {},
      'FlcTcpSocket',
      'closeServer',
      [this.id]
    );
    this._close();
  }
};

FlcTcpServer.prototype.getClient = function(id) {
  for (let i = 0; i < this.clients.length; i++) {
    const client = this.clients[i];
    if (client.id === id) {
      return client;
    }
  }
  return null;
};

FlcTcpServer.prototype._close = function() {
  if (!this.isClosed) {
    this.isClosed = true;
    const this_ = this;
    this.clients.splice().forEach(function(client) {
      this_._closeClient(client);
    });
    if (this.onClose) {
      this.onClose();
    }
  }
};

FlcTcpServer.prototype._addClient = function(client) {
  this.clients.push(client);
  const this_ = this;
  client._onClose = function() {
    const index = this_.clients.indexOf(client);
    if (index !== -1) {
      this_.clients.splice(index, 1);
    }
  };
};

FlcTcpServer.prototype._closeClient = function(idOrClient) {
  const client = idOrClient instanceof FlcTcpClient ? idOrClient : this.getClient(idOrClient);
  if (client) {
    client._close();
  }
};

function FlcTcpClient(id, address) {
  this.id = id;
  this.address = address;
  this.isClosed = false;
  this.onClose = null;
  this._onClose = null;
}

FlcTcpClient.prototype.receive = function(dataCallback, errorCallback) {
  if (this.isClosed) {
    if (errorCallback) {
      errorCallback('client closed');
    }
    return;
  }
  const waitingPayloads = new Map();
  let lastOrderNo = -1;
  exec(
    function(payload) {
      if (dataCallback) {
        payload = {
          data: payload.slice(4),
          orderNo: new DataView(payload).getInt32(0)
        };
        if (payload.orderNo === lastOrderNo + 1) {
          dataCallback(payload);
          lastOrderNo = payload.orderNo;
          checkWaitingPayload();
          return;
        }
        waitingPayloads.set(payload.orderNo, payload);
      }
    },
    function(message) {
      if (errorCallback) {
        errorCallback(message);
      }
    },
    'FlcTcpSocket',
    'clientReceive',
    [this.id]
  );
  function checkWaitingPayload() {
    const payload = waitingPayloads.get(lastOrderNo + 1);
    if (payload) {
      dataCallback(payload);
      lastOrderNo = payload.orderNo;
      waitingPayloads.delete(payload.orderNo);
      checkWaitingPayload();
    }
  }
};

FlcTcpClient.prototype.close = function() {
  if (!this.isClosed) {
    exec(
      function() {},
      function() {},
      'FlcTcpSocket',
      'closeClient',
      [this.id]
    );
    this._close();
  }
};

FlcTcpClient.prototype._close = function() {
  if (!this.isClosed) {
    this.isClosed = true;
    this._onClose();
    if (this.onClose) {
      this.onClose();
    }
  }
};

module.exports = FlcTcpSocket;
