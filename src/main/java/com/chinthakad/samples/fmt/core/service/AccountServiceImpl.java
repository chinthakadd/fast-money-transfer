package com.chinthakad.samples.fmt.core.service;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.domain.Transfer;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.core.repository.AccountRepository;
import com.chinthakad.samples.fmt.core.repository.AccountRepositoryImpl;
import com.chinthakad.samples.fmt.core.repository.TransferRepository;
import com.chinthakad.samples.fmt.core.repository.TransferRepositoryImpl;
import com.chinthakad.samples.fmt.seedwork.FmtException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author Chinthaka D
 */
@Slf4j
public class AccountServiceImpl implements AccountService {

  private EventBus eventBus;
  private AccountRepository accountRepository;
  private TransferRepository transferRepository;

  public AccountServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
    this.accountRepository = new AccountRepositoryImpl(eventBus);
    this.transferRepository = new TransferRepositoryImpl(eventBus);
  }

  @Override
  public Future<AccountListHolder> getAccounts() {
    return accountRepository.findAll();
  }

  @Override
  public Future<TransferListHolder> getTransfers() {
    return transferRepository.findAll();
  }

  @Override
  public Future<Void> transferMoney(TransferRequestDto transferRequest) {
    Promise<Void> alhPromise = Promise.promise();
    log.info("Transfer Request: {}", transferRequest);
    String fromAccountStr = transferRequest.getFromAccountNumber();
    String toAccountStr = transferRequest.getToAccountNumber();
    BigDecimal transferAmount = transferRequest.getAmount();

    // NOTE: Getting all accounts for simplicity for now.
    getAccounts()
      .onSuccess(
        holder -> {
          log.info("Accounts: {}", holder);

          // Get From Account and To Account.
          Account fromAccount = holder.getAccounts().stream()
            .filter(account -> account.getAccountNumber().equals(fromAccountStr)).findFirst().orElseThrow(
              () -> new FmtException("From Account Not Found")
            );
          Account toAccount = holder.getAccounts().stream()
            .filter(account -> account.getAccountNumber().equals(toAccountStr)).findFirst().orElseThrow(
              () -> new FmtException("To Account Not Found")
            );

          Transfer.builder().fromAccount(fromAccount).toAccount(toAccount).amount(transferAmount).build()
            .withEventBus(eventBus)
            .initiateTransfer()
            .setHandler(event -> alhPromise.complete());
        }
      );
    return alhPromise.future();
  }


}
