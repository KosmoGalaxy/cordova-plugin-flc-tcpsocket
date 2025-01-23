package com.example.tcp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executors;

import pl.fulllegitcode.tcpsocket.FlcTcpSocketClient;

public class MainActivity extends AppCompatActivity {

  private FlcTcpSocketClient socket;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);
    testSocket();
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });
  }

  private void testSocket() {
    socket = new FlcTcpSocketClient(Executors.newCachedThreadPool());
    socket.connect("172.20.10.8", 36900);
    socket.receive(new FlcTcpSocketClient.ReceiveCallback() {
      @Override
      public void onDataReceived(byte[] data) {
        Log.d("MainActivity", "Data received: " + new String(data));
      }

      @Override
      public void onError(String errorMessage) {
        Log.e("MainActivity", "Error: " + errorMessage);
      }
    });
    socket.close();
  }
}