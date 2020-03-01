package com.chinthakad.samples.fmt.seedwork.codec;

import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;

public class AccountCommandCodec extends LocalOnlyMessageCodec<AccountSvcMessage, AccountSvcMessage> {

  @Override
  public AccountSvcMessage transform(AccountSvcMessage accountDataRequest) {
    return accountDataRequest;
  }

  @Override
  public String name() {
    return "AccountCommandCodec";
  }
}
