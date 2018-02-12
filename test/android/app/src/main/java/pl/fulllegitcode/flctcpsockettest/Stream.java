package pl.fulllegitcode.flctcpsockettest;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

public class Stream {

  class Chunk {
    public byte[] size = new byte[4];
    public byte[] data;
    public int sizePosition;
    public int dataPosition;
  }


  private ArrayList<Chunk> _chunks = new ArrayList<>();
  private Chunk _openChunk;

  public void writeBytes(byte[] bytes) {
    writeBytes(bytes, 0);
  }

  public void writeBytes(byte[] bytes, int offset) {
    if (_openChunk == null) {
      _openChunk = new Chunk();
    }
    Chunk chunk = _openChunk;
    if (chunk.sizePosition < chunk.size.length) {
      int numBytesCopied = _copy(bytes, offset, chunk.size, chunk.sizePosition);
      chunk.sizePosition += numBytesCopied;
      offset += numBytesCopied;
//      int size = chunk.size[0] << 24 | (chunk.size[1] & 0xff) << 16 | (chunk.size[2] & 0xff) << 8 | (chunk.size[3] & 0xff);
//      Log.d("FlcTcpSocketTest", String.format(Locale.ENGLISH, "-- numBytesCopied=%d bytes=%d %d %d %d size=%d", numBytesCopied, chunk.size[0], chunk.size[1], chunk.size[2], chunk.size[3], size));
    }
    if (chunk.sizePosition == chunk.size.length && chunk.data == null) {
      int size = chunk.size[0] << 24 | (chunk.size[1] & 0xff) << 16 | (chunk.size[2] & 0xff) << 8 | (chunk.size[3] & 0xff);
      chunk.data = new byte[size];
    }
    if (chunk.dataPosition < chunk.data.length) {
      int numBytesCopied = _copy(bytes, offset, chunk.data, chunk.dataPosition);
      chunk.dataPosition += numBytesCopied;
      offset += numBytesCopied;
    }
    if (chunk.dataPosition == chunk.data.length) {
      _chunks.add(chunk);
      _openChunk = null;
      if (offset < bytes.length) {
        writeBytes(bytes, offset);
      }
    }
  }

  public byte[] readData() {
    if (_chunks.size() != 0) {
      return _chunks.remove(0).data;
    }
    return null;
  }

  private int _copy(byte[] source, int sourceOffset, byte[] target, int targetOffset) {
    int numBytesCopied = Math.min(source.length - sourceOffset, target.length - targetOffset);
    for (int i = 0; i < numBytesCopied; i++) {
      target[targetOffset + i] = source[sourceOffset + i];
    }
    return numBytesCopied;
  }

}
