package pl.fulllegitcode.tcpsocket;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class FlcTcpClient {

  public class ReceiveException extends Exception {
    public ReceiveException(String message) { super(message); }
  }


  public interface LifeCycleCallback {
    void onClose();
  }


  public interface ReceiveCallback {
    void onData(byte[] data);
  }


  private static int _nextId = 1;


  private int _id = _nextId++;
  public int id() { return _id; }

  private boolean _isClosed = false;
  public boolean isClosed() { return _isClosed; }

  private boolean _isReceiving = false;
  public boolean isReceiving() { return _isReceiving; }

  private ExecutorService _threadPool;
  public ExecutorService threadPool() { return _threadPool; }

  private SocketChannel _channel;
  public SocketChannel channel() { return _channel; }

  private InetAddress _address;
  public InetAddress address() { return _address; }

  private ArrayList<LifeCycleCallback> _lifeCycleCallbacks = new ArrayList<LifeCycleCallback>();
  private ArrayList<LifeCycleCallback> lifeCycleCallbacks() { return _lifeCycleCallbacks; }

  public FlcTcpClient(ExecutorService threadPool, SocketChannel channel) {
    _threadPool = threadPool;
    _channel = channel;
    _address = channel.socket().getInetAddress();
  }

  public void receive(final ReceiveCallback callback) throws FlcTcpSocket.LifeCycleException, ReceiveException {
    if (isClosed()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "client receive error. id=%d message=closed", id()));
      throw new FlcTcpSocket.LifeCycleException("closed");
    }
    if (isReceiving()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "client receive error. id=%d message=already receiving", id()));
      throw new ReceiveException("already receiving");
    }
    threadPool().execute(new Runnable() {
      @Override
      public void run() {
        try {
          ByteBuffer buffer = ByteBuffer.allocate(1024);
          int numBytesRead = 0;
          while (!isClosed() && numBytesRead != -1) {
            numBytesRead = channel().read(buffer);
            if (numBytesRead > 0) {
              byte[] data = new byte[numBytesRead];
              buffer.position(0);
              buffer.get(data, 0, numBytesRead);
              buffer.clear();
              FlcTcpSocket.logDebug(String.format(Locale.ENGLISH, "client received. id=%d size=%d", id(), numBytesRead));
              callback.onData(data);
            }
          }
        } catch (Exception e) {
          FlcTcpSocket.logError(String.format(Locale.ENGLISH, "client error. id=%d message=%s", id(), e.getMessage()));
        }
        _close();
      }
    });
    FlcTcpSocket.logInfo(String.format(Locale.ENGLISH, "client receiving. id=%d address=%s", id(), address()));
    _isReceiving = true;
  }

  public void close() throws FlcTcpSocket.LifeCycleException {
    if (isClosed()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "client close error. id=%d message=already closed", id()));
      throw new FlcTcpSocket.LifeCycleException("already closed");
    }
    _close();
  }

  public void addLifeCycleCallback(LifeCycleCallback callback) {
    lifeCycleCallbacks().add(callback);
  }

  private void _close() {
    if (!isClosed()) {
      _isClosed = true;
      try {
        channel().close();
      } catch (IOException e) {
        FlcTcpSocket.logError(String.format(Locale.ENGLISH, "client close error. id=%d message=%s", id(), e.getMessage()));
      }
      FlcTcpSocket.logInfo(String.format(Locale.ENGLISH, "client closed. id=%d address=%s", id(), address()));
      _callLifeCycleClose();
    }
  }

  private void _callLifeCycleClose() {
    for (int i = 0; i < lifeCycleCallbacks().size(); i++) {
      LifeCycleCallback callback = lifeCycleCallbacks().get(i);
      callback.onClose();
    }
  }

}
