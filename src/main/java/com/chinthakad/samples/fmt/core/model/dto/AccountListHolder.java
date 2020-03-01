package com.chinthakad.samples.fmt.core.model.dto;

import com.chinthakad.samples.fmt.core.model.BaseModel;
import com.chinthakad.samples.fmt.core.model.domain.Account;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@ToString
public class AccountListHolder implements BaseModel {

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
