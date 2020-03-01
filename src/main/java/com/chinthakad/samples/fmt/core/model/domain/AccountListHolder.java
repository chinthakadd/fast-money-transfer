package com.chinthakad.samples.fmt.core.model.domain;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@ToString
public class AccountListHolder {

  private List<Account> accounts = new ArrayList<>();

  public AccountListHolder withAccounts(List<Account> accountsArg) {
    this.accounts.addAll(accountsArg);
    return this;
  }

  public AccountListHolder withAccounts(Account... accountsArg) {
    this.accounts.addAll(Arrays.asList(accountsArg));
    return this;
  }
}
