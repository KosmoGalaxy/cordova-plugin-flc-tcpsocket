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
  public interface OpenCallback {
    void onClose();
  }

  public interface ReceiveCallback {
    void onDataReceived(byte[] data);
  }

  private static int _nextId = 1;

  private final String TAG = "FlcTcpSocketClient";

  private final int _id = _nextId++;
  private Socket socket;
  private OutputStream outputStream;
  private InputStream inputStream;
  private ExecutorService threadPool;
  private OpenCallback openCallback;

  public int id() { return _id; }

  public FlcTcpSocketClient(ExecutorService threadPool) {
    this.threadPool = threadPool;
  }

  public void connect(String ip, int port, OpenCallback callback) throws IOException {
    try {
      openCallback = callback;
      socket = new Socket(ip, port);
      inputStream = new BufferedInputStream(socket.getInputStream());
      outputStream = new BufferedOutputStream(socket.getOutputStream());
      Log.d(TAG, "connected: " + ip + ":" + port);
      threadPool.execute(() -> {
        while (!socket.isClosed()) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException ignored) {
          }
        }
        openCallback.onClose();
      });
    } catch (IOException e) {
      Log.d(TAG, "connect exception: " + e);
      throw e;
    }
  }

  public void receive(ReceiveCallback receiveCallback) {
    threadPool.execute(() -> {
      while (!socket.isClosed()) {
        try {
          Thread.sleep(10);
          if (inputStream.available() == 0)
            continue;
          byte[] buffer = new byte[inputStream.available()];
          int bytesRead = inputStream.read(buffer);
          if (bytesRead == 0) {
            close();
            return;
          }
          receiveCallback.onDataReceived(buffer);
        } catch (Exception e) {
          close();
          return;
        }
      }
    });
  }

  public void send(byte[] data) {
    try {
      outputStream.write(data);
      outputStream.flush();
    } catch (IOException e) {
      Log.d(TAG, "send exception: " + e);
      close();
    }
  }

  public void close() {
    try {
      socket.close();
    } catch (IOException e) {
      Log.d(TAG, "close exception: " + e);
    }
  }
}
