package com.chinthakad.samples.fmt.seedwork.codec;

import com.chinthakad.samples.fmt.seedwork.queue.AccountJdbcMessage;

public class AccountDataRequestCodec extends LocalOnlyMessageCodec<AccountJdbcMessage, AccountJdbcMessage> {

  @Override
  public AccountJdbcMessage transform(AccountJdbcMessage accountJdbcMessage) {
    return accountJdbcMessage;
  }

  @Override
  public String name() {
    return "AccountDataRequestCodec";
  }
}
