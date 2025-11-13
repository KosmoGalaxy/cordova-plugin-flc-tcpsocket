package pl.fulllegitcode.tcpsocket;

import android.util.Base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

public class FlcTcpSocketPlugin extends CordovaPlugin {

  public class Servers extends ArrayList<FlcTcpServer> {}


  private static final String ACTION_SET_DEBUG = "setDebug";
  private static final String ACTION_OPEN_SERVER = "openServer";
  private static final String ACTION_CLOSE_SERVER = "closeServer";
  private static final String ACTION_CLIENT_SEND = "clientSend";
  private static final String ACTION_CLIENT_RECEIVE = "clientReceive";
  private static final String ACTION_CLOSE_CLIENT = "closeClient";
  private static final String ACTION_OPEN_SOCKET = "openSocket";
  private static final String ACTION_SOCKET_RECEIVE = "socketReceive";
  private static final String ACTION_SOCKET_SEND = "socketSend";
  private static final String ACTION_SOCKET_CLOSE = "socketClose";


  private Servers _servers = new Servers();
  public Servers servers() { return _servers; }

  private ArrayList<FlcTcpSocketClient> sockets = new ArrayList<FlcTcpSocketClient>();


  @Override
  public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
    if (action.equals(ACTION_SET_DEBUG)) {
      FlcTcpSocket.debug = args.getBoolean(0);
      callbackContext.success();
      return true;
    }
    if (action.equals(ACTION_OPEN_SERVER)) {
      _openServer(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_CLOSE_SERVER)) {
      _closeServer(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_CLIENT_SEND)) {
      _clientSend(args.getInt(0), args.getArrayBuffer(1), callbackContext);
      return true;
    }
    if (action.equals(ACTION_CLIENT_RECEIVE)) {
      _clientReceive(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_CLOSE_CLIENT)) {
      _closeClient(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_OPEN_SOCKET)) {
      openSocket(args.getString(0), args.getInt(1), callbackContext);
      return true;
    }
    if (action.equals(ACTION_SOCKET_RECEIVE)) {
      socketReceive(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_SOCKET_SEND)) {
      socketSend(args.getInt(0), args.getArrayBuffer(1), callbackContext);
      return true;
    }
    if (action.equals(ACTION_SOCKET_CLOSE)) {
      closeSocket(args.getInt(0), callbackContext);
      return true;
    }
    return false;
  }


  public void openSocket(String ip, int port, CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
      try {
        FlcTcpSocketClient socket = new FlcTcpSocketClient(cordova.getThreadPool());
        socket.connect(ip, port, new FlcTcpSocketClient.OpenCallback() {
          @Override
          public void onClose() {
            try {
              sockets.remove(socket);
              JSONObject payload = new JSONObject();
              payload.put("event", "close");
              callbackContext.success(payload);
            } catch (JSONException e) {
              FlcTcpSocket.logError(String.format(Locale.ENGLISH, "json error. message=%s", e.getMessage()));
            }
          }
        });
        sockets.add(socket);
        JSONObject payload = new JSONObject();
        payload.put("event", "open");
        payload.put("id", socket.id());
        PluginResult result = new PluginResult(PluginResult.Status.OK, payload);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
      } catch (Exception e) {
        callbackContext.error(e.getMessage());
      }
      }
    });
  }

  public void socketReceive(final int id, CallbackContext callbackContext) {
    FlcTcpSocketClient socket = getSocket(id);
    if (socket == null) {
      callbackContext.error("socket not found");
      return;
    }
    socket.receive(new FlcTcpSocketClient.ReceiveCallback() {
      @Override
      public void onDataReceived(byte[] bytes) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, bytes);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
      }
    });
  }

  public void socketSend(final int id, byte[] bytes, CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        FlcTcpSocketClient socket = getSocket(id);
        if (socket == null) {
          callbackContext.error("socket not found");
          return;
        }
        socket.send(bytes);
        callbackContext.success(bytes);
      }
    });
  }

  public void closeSocket(final int id, CallbackContext callbackContext) {
    FlcTcpSocketClient socket = getSocket(id);
    if (socket == null) {
      callbackContext.error("socket not found");
      return;
    }
    socket.close();
    callbackContext.success();
  }

  public FlcTcpSocketClient getSocket(int id) {
    for (int i = 0; i < sockets.size(); i++) {
      FlcTcpSocketClient socket = sockets.get(i);
      if (socket.id() == id) {
        return socket;
      }
    }
    return null;
  }

  public FlcTcpServer getServer(int id) {
    for (int i = 0; i < servers().size(); i++) {
      FlcTcpServer server = servers().get(i);
      if (server.id() == id) {
        return server;
      }
    }
    return null;
  }

  public FlcTcpClient getClient(int id) {
    for (int i = 0; i < servers().size(); i++) {
      FlcTcpServer server = servers().get(i);
      FlcTcpClient client = server.getClient(id);
      if (client != null) {
        return client;
      }
    }
    return null;
  }

  private void _openServer(final int port, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        final FlcTcpServer server = new FlcTcpServer(cordova.getThreadPool());
        try {
          server.open(port, new FlcTcpServer.OpenCallback() {
            @Override
            public void onClient(FlcTcpClient client) {
              _bindClientLifeCycle(client, callbackContext);
              try {
                JSONObject payload = new JSONObject();
                payload.put("event", "client");
                payload.put("id", client.id());
                payload.put("address", client.address().toString().replaceAll("/", ""));
                PluginResult result = new PluginResult(PluginResult.Status.OK, payload);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (JSONException e) {
                FlcTcpSocket.logError(String.format(Locale.ENGLISH, "json error. message=%s", e.getMessage()));
              }
            }
            @Override
            public void onClose() {
              servers().remove(server);
              try {
                JSONObject payload = new JSONObject();
                payload.put("event", "close");
                callbackContext.success(payload);
              } catch (JSONException e) {
                FlcTcpSocket.logError(String.format(Locale.ENGLISH, "json error. message=%s", e.getMessage()));
              }
            }
            @Override
            public void onError(String message) {
              try {
                JSONObject payload = new JSONObject();
                payload.put("event", "error");
                payload.put("message", message);
                PluginResult result = new PluginResult(PluginResult.Status.OK, payload);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              } catch (JSONException e) {
                FlcTcpSocket.logError(String.format(Locale.ENGLISH, "json error. message=%s", e.getMessage()));
              }
            }
          });
          servers().add(server);
          JSONObject payload = new JSONObject();
          payload.put("event", "open");
          payload.put("id", server.id());
          PluginResult result = new PluginResult(PluginResult.Status.OK, payload);
          result.setKeepCallback(true);
          callbackContext.sendPluginResult(result);
        } catch (Exception e) {
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void _bindClientLifeCycle(final FlcTcpClient client, final CallbackContext callbackContext) {
    client.addLifeCycleCallback(new FlcTcpClient.LifeCycleCallback() {
      @Override
      public void onClose() {
        try {
          JSONObject payload = new JSONObject();
          payload.put("event", "clientClose");
          payload.put("id", client.id());
          PluginResult result = new PluginResult(PluginResult.Status.OK, payload);
          result.setKeepCallback(true);
          callbackContext.sendPluginResult(result);
        } catch (JSONException e) {
          FlcTcpSocket.logError(String.format(Locale.ENGLISH, "json error. message=%s", e.getMessage()));
        }
      }
    });
  }

  private void _closeServer(final int id, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        FlcTcpServer server = getServer(id);
        if (server != null) {
          try {
            server.close();
            callbackContext.success();
          } catch (FlcTcpSocket.LifeCycleException e) {
            callbackContext.error(e.getMessage());
          }
        } else {
          callbackContext.error("server not found");
        }
      }
    });
  }

  private void _clientSend(final int id, final byte[] data, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        FlcTcpClient client = getClient(id);
        if (client != null) {
          try {
            client.sendSync(data);
            callbackContext.success();
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        } else {
          callbackContext.error("client not found");
        }
      }
    });
  }

  private void _clientReceive(final int id, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        final int[] orderNo = {0};
        FlcTcpClient client = getClient(id);
        if (client != null) {
          try {
            client.receive(new FlcTcpClient.ReceiveCallback() {
              @Override
              public void onData(byte[] data) {
                ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
                buffer.putInt(orderNo[0]++);
                buffer.put(data);
                PluginResult result = new PluginResult(PluginResult.Status.OK, buffer.array());
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
              }
            });
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        } else {
          callbackContext.error("client not found");
        }
      }
    });
  }

  private void _closeClient(final int id, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        FlcTcpClient client = getClient(id);
        if (client != null) {
          try {
            client.close();
            callbackContext.success();
          } catch (FlcTcpSocket.LifeCycleException e) {
            callbackContext.error(e.getMessage());
          }
        } else {
          callbackContext.error("client not found");
        }
      }
    });
  }

}
