package pl.fulllegitcode.tcpsocket;

import android.util.Log;

public class FlcTcpSocket {

  public static class LifeCycleException extends Exception {
    public LifeCycleException(String message) { super(message); }
  }


  private static final String TAG = "FlcTcpSocket";

  public static boolean debug = false;

  public static void logInfo(String message) {
    Log.d(TAG, message);
  }

  public static void logError(String message) {
    Log.e(TAG, message);
  }

  public static void logDebug(String message) {
    if (debug) {
      Log.d(TAG, message);
    }
  }

}
