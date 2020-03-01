package com.chinthakad.samples.fmt.core.service;

import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import io.vertx.core.Future;

public interface AccountService {
  Future<AccountListHolder> getAccounts();
  Future<TransferListHolder> getTransfers();
  Future<Void> transferMoney(TransferRequestDto transferRequest);
}
