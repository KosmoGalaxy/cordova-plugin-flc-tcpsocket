/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
let app = {
  // Application Constructor
  initialize: function() {
    document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
  },

  // deviceready Event Handler
  //
  // Bind any cordova events here. Common events are:
  // 'pause', 'resume', etc.
  onDeviceReady: function() {
    this.receivedEvent('deviceready');
    test();
  },

  // Update DOM on a Received Event
  receivedEvent: function(id) {
    let parentElement = document.getElementById(id);
    let listeningElement = parentElement.querySelector('.listening');
    let receivedElement = parentElement.querySelector('.received');

    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');

    console.log('Received Event: ' + id);
  }
};

app.initialize();


function test() {
  cordova.plugins['FlcTcpSocket'].setDebug(true);
  let openServerButton = document.getElementById('button-open-server');
  openServerButton.onclick = () => {
    cordova.plugins['FlcTcpSocket'].openServer(
      3070,
      server => {
        console.log('server open', server);
        onServer(server);
      },
      message => {
        console.error(message);
      }
    );
  };
}

function onServer(server) {
  let closeButton = document.getElementById('button-close-server');
  closeButton.onclick = () => {
    server.close();
  };
  server.onClient = client => {
    console.log('client open', client);
    onClient(client);
  };
  server.onClose = () => console.log('server closed');
}

function onClient(client) {
  client.receive(
    payload => {
      if (window.TextDecoder) {
        const decoder = new TextDecoder('utf-8');
        console.log('--', decoder.decode(new Uint8Array(payload.data)));
      } else {
        console.log('--', payload.data);
      }
    },
    message => {
      console.error(message);
    }
  );
  client.onClose = () => console.log('client closed');
}
