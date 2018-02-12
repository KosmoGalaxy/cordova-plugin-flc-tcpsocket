package pl.fulllegitcode.flctcpsockettest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.nio.ByteBuffer;
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
      final Stream stream = new Stream();
      final boolean[] wasMetaReceived = new boolean[1];
      client.receive(new FlcTcpClient.ReceiveCallback() {
        @Override
        public void onData(byte[] bytes) {
          stream.writeBytes(bytes);
          byte[] data = stream.readData();
          if (data != null) {
            if (!wasMetaReceived[0]) {
              _testDecodeMeta(data);
              wasMetaReceived[0] = true;
            } else {
              _testDecodeImage(data);
            }
          }
        }
      });
    } catch (Exception e) {}
  }

  private void _testDecodeMeta(byte[] data) {
    Log.d("FlcTcpSocketTest", String.format(Locale.ENGLISH, "meta. text=%s", new String(data, Charset.forName("UTF-8"))));
  }

  private void _testDecodeImage(byte[] data) {
    Log.d("FlcTcpSocketTest", String.format(Locale.ENGLISH, "image. size=%d", data.length));
    ByteBuffer buffer = ByteBuffer.wrap(data);
    final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        ImageView image = findViewById(R.id.imageView2);
        image.setImageBitmap(bitmap);
      }
    });
  }

}
