package pl.fulllegitcode.tcpsocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class FlcTcpServer {

  public class Clients extends ArrayList<FlcTcpClient> {}


  public interface OpenCallback {
    void onClient(FlcTcpClient client);
    void onClose();
    void onError(String message);
  }


  private static int _nextId = 1;


  private int _id = _nextId++;
  public int id() { return _id; }

  private boolean _isOpen = false;
  public boolean isOpen() { return _isOpen; }

  private boolean _isClosed = false;
  public boolean isClosed() { return _isClosed; }

  private ExecutorService _threadPool;
  public ExecutorService threadPool() { return _threadPool; }

  private ServerSocketChannel _channel;
  public ServerSocketChannel channel() { return _channel; }

  private Clients _clients = new Clients();
  public Clients clients() { return _clients; }

  private OpenCallback _openCallback;
  private OpenCallback openCallback() { return _openCallback; }

  public FlcTcpServer(ExecutorService threadPool) {
    _threadPool = threadPool;
  }

  public void open(int port, OpenCallback callback) throws FlcTcpSocket.LifeCycleException, IOException {
    if (isClosed()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server open error. id=%d message=closed", id()));
      throw new FlcTcpSocket.LifeCycleException("closed");
    }
    if (isOpen()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server open error. id=%d message=already open", id()));
      throw new FlcTcpSocket.LifeCycleException("already open");
    }
    try {
      _channel = ServerSocketChannel.open();
      channel().socket().bind(new InetSocketAddress(port));
      threadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            while (!isClosed()) {
              SocketChannel clientChannel = channel().accept();
              _addClient(clientChannel);
            }
          } catch (IOException e) {
            FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server error. id=%d message=%s", id(), e.getMessage()));
            openCallback().onError(e.getMessage());
            _close();
          }
        }
      });
      _openCallback = callback;
      _isOpen = true;
      FlcTcpSocket.logInfo(String.format(Locale.ENGLISH, "server open. id=%d port=%d", id(), _channel.socket().getLocalPort()));
    } catch (IOException e) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server open error. id=%d message=%s", id(), e.getMessage()));
      throw e;
    }
  }

  public void close() throws FlcTcpSocket.LifeCycleException {
    if (isClosed()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server close error. id=%d message=already closed", id()));
      throw new FlcTcpSocket.LifeCycleException("already closed");
    }
    if (!isOpen()) {
      FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server close error. id=%d message=not open", id()));
      throw new FlcTcpSocket.LifeCycleException("not open");
    }
    _close();
  }

  public FlcTcpClient getClient(int id) {
    for (int i = 0; i < clients().size(); i++) {
      FlcTcpClient client = clients().get(i);
      if (client.id() == id) {
        return client;
      }
    }
    return null;
  }

  private void _addClient(SocketChannel channel) {
    final FlcTcpClient client = new FlcTcpClient(threadPool(), channel);
    client.addLifeCycleCallback(new FlcTcpClient.LifeCycleCallback() {
      @Override
      public void onClose() {
        clients().remove(client);
      }
    });
    clients().add(client);
    FlcTcpSocket.logInfo(String.format(Locale.ENGLISH, "server client connected. id=%d clientId=%d address=%s numAllClients=%d", id(), client.id(), client.address(), clients().size()));
    openCallback().onClient(client);
  }

  private void _close() {
    if (!isClosed()) {
      _isClosed = true;
      try {
        channel().close();
      } catch (IOException e) {
        FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server close error. id=%d message=%s", id(), e.getMessage()));
      }
      try {
        _closeAllClients();
      } catch (Exception e) {
        FlcTcpSocket.logError(String.format(Locale.ENGLISH, "server close error. id=%d message=%s", id(), e.getMessage()));
      }
      FlcTcpSocket.logInfo(String.format(Locale.ENGLISH, "server closed. id=%d", id()));
      openCallback().onClose();
    }
  }

  private void _closeAllClients() {
    Clients clients = (Clients) clients().clone();
    for (int i = 0; i < clients.size(); i++) {
      try {
        clients.get(i).close();
      } catch (FlcTcpSocket.LifeCycleException e) {}
    }
  }

}
