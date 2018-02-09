package pl.fulllegitcode.flctcpsockettest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.Executors;

import pl.fulllegitcode.tcpsocket.FlcTcpClient;
import pl.fulllegitcode.tcpsocket.FlcTcpServer;
import pl.fulllegitcode.tcpsocket.FlcTcpSocket;

public class MainActivity extends AppCompatActivity {

  private FlcTcpServer _server;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    _test();
  }

  private void _test() {
    FlcTcpSocket.debug = true;
    _testServer();
  }

  private void _testServer() {
    try {
      _server = new FlcTcpServer(Executors.newCachedThreadPool());
      _server.open(3070, new FlcTcpServer.OpenCallback() {
        @Override
        public void onClient(FlcTcpClient client) {
          _testClientLifeCycle(client);
          _testClientReceive(client);
        }
        @Override
        public void onClose() {
//          Log.d("FlcTcpSocketTest", "server closed");
        }
        @Override
        public void onError(String message) {
//          Log.e("FlcTcpSocketTest", String.format(Locale.ENGLISH, "server error. message=%s", message));
        }
      });
    } catch (Exception e) {}
  }

  private void _testClientLifeCycle(final FlcTcpClient client) {
    client.addLifeCycleCallback(new FlcTcpClient.LifeCycleCallback() {
      @Override
      public void onClose() {
//        Log.d("FlcTcpSocketTest", String.format(Locale.ENGLISH, "client closed. id=%d address=%s", client.id(), client.address()));
      }
    });
  }

  private void _testClientReceive(final FlcTcpClient client) {
    try {
      client.receive(new FlcTcpClient.ReceiveCallback() {
        @Override
        public void onData(byte[] data) {
          //Log.d("FlcTcpSocketTest", String.format(Locale.ENGLISH, "client received. id=%d text=%s", client.id(), new String(data, Charset.forName("UTF-8"))));
        }
      });
    } catch (Exception e) {}
  }

}
