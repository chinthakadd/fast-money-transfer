package com.chinthakad.samples.fmt.seedwork.codec;

import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;

public class AccountListHolderMessageCodec extends LocalOnlyMessageCodec<AccountListHolder, AccountListHolder> {


  @Override
  public AccountListHolder transform(AccountListHolder accounts) {
    return accounts;
  }

  @Override
  public String name() {
    return "AccountListCodec";
  }

}
