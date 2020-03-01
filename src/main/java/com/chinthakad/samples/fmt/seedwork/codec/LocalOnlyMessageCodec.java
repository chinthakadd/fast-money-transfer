package com.chinthakad.samples.fmt.seedwork.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public abstract class LocalOnlyMessageCodec<S, R> implements MessageCodec<S, R> {
  @Override
  public void encodeToWire(Buffer buffer, S s) {
    // do nothing
  }

  @Override
  public R decodeFromWire(int pos, Buffer buffer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
