package pl.fulllegitcode.tcpsocket;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class FlcTcpSocketPlugin extends CordovaPlugin {

  public class Servers extends ArrayList<FlcTcpServer> {}


  private static final String ACTION_SET_DEBUG = "setDebug";
  private static final String ACTION_OPEN_SERVER = "openServer";
  private static final String ACTION_CLOSE_SERVER = "closeServer";
  private static final String ACTION_CLIENT_RECEIVE = "clientReceive";
  private static final String ACTION_CLOSE_CLIENT = "closeClient";


  private Servers _servers = new Servers();
  public Servers servers() { return _servers; }

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
    if (action.equals(ACTION_CLIENT_RECEIVE)) {
      _clientReceive(args.getInt(0), callbackContext);
      return true;
    }
    if (action.equals(ACTION_CLOSE_CLIENT)) {
      _closeClient(args.getInt(0), callbackContext);
      return true;
    }
    return false;
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

  private void _clientReceive(final int id, final CallbackContext callbackContext) {
    cordova.getThreadPool().execute(new Runnable() {
      @Override
      public void run() {
        FlcTcpClient client = getClient(id);
        if (client != null) {
          try {
            client.receive(new FlcTcpClient.ReceiveCallback() {
              @Override
              public void onData(byte[] data) {
                PluginResult result = new PluginResult(PluginResult.Status.OK, data);
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
