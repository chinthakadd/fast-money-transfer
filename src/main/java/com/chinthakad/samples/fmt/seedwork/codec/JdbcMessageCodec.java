package com.chinthakad.samples.fmt.seedwork.codec;

import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;

public class JdbcMessageCodec extends LocalOnlyMessageCodec<JdbcClientMessage, JdbcClientMessage> {

  @Override
  public JdbcClientMessage transform(JdbcClientMessage jdbcClientMessage) {
    return jdbcClientMessage;
  }

  @Override
  public String name() {
    return "AccountDataRequestCodec";
  }
}
