package pl.fulllegitcode.tcpsocket;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class FlcTcpSocketClient {
  private String TAG = "FlcTcpSocketClient";

  private Socket socket;
  private OutputStream outputStream;
  private InputStream inputStream;
  private boolean isConnected = false;

  private ExecutorService _threadPool;
  public ExecutorService threadPool() {
    return _threadPool;
  }

  public interface ReceiveCallback {
    void onDataReceived(byte[] data);
    void onError(String error);
  }

  public FlcTcpSocketClient(ExecutorService threadPool) {
    this._threadPool = threadPool;
  }

  public void connect(String ip, int port) {
    threadPool().execute(() -> {
      try {
        socket = new Socket(ip, port);
        inputStream = new BufferedInputStream(socket.getInputStream());
        outputStream = new BufferedOutputStream(socket.getOutputStream());
        isConnected = true;
        Log.d(TAG, "Connected to: " + ip + ":" + port);
      } catch (IOException e) {
        Log.d(TAG, "connect exception: " + e);
      }
    });
  }

  public void receive(ReceiveCallback receiveCallback) {
    threadPool().execute(() -> {
      try {
        while (true) {
          if (inputStream.available() > 0) {
            byte[] buffer = new byte[inputStream.available()];
            int bytesRead = inputStream.read(buffer);

            if (bytesRead == 0) {
              receiveCallback.onError("Bytes read is 0");
            }

            if (receiveCallback != null) {
              receiveCallback.onDataReceived(buffer);
            }
          }
        }
      } catch (IOException e) {
        Log.e(TAG, "[Client] Error while reading from socket: " + e);
        if (receiveCallback != null) {
          receiveCallback.onError("Receive exception: " + e);
        }
      }
    });
  }

  public void send(byte[] data) {
    threadPool().execute(() -> {
      try {
        if (isConnected && outputStream != null) {
          outputStream.write(data);
          outputStream.flush();
        } else {
          Log.d(TAG, "Socket is not connected");
        }
      } catch (IOException e) {
        Log.d(TAG, "send exception: " + e);
      }
    });
  }

  public void close() {
    try {
      if (isConnected) {
        isConnected = false;
        if (socket != null) {
          socket.close();
        }
      }
    } catch (IOException e) {
      Log.d(TAG, "close exception: " + e);
    }
  }
}
