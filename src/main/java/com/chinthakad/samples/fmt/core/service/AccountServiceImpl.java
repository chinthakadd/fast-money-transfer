package com.chinthakad.samples.fmt.core.service;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.core.repository.AccountRepository;
import com.chinthakad.samples.fmt.core.repository.AccountRepositoryImpl;
import com.chinthakad.samples.fmt.seedwork.FmtException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class AccountServiceImpl implements AccountService {

  private EventBus eventBus;
  private AccountRepository accountRepository;

  public AccountServiceImpl(EventBus eventBus) {
    this.eventBus = eventBus;
    this.accountRepository = new AccountRepositoryImpl(eventBus);
  }

  @Override
  public Future<AccountListHolder> getAccounts() {
    return accountRepository.findAll();
  }

  @Override
  public Future<Void> transferMoney(TransferRequestDto transferRequest) {
    Promise<Void> alhPromise = Promise.promise();
    log.info("Transfer Request: {}", transferRequest);
    String fromAccountStr = transferRequest.getFromAccountNumber();
    String toAccountStr = transferRequest.getToAccountNumber();
    BigDecimal transferAmount = transferRequest.getAmount();
    getAccounts()
      .onSuccess(
        holder -> {
          log.info("Accounts: {}", holder);
          Account fromAccount = holder.getAccounts().stream()
            .filter(account -> account.getAccountNumber().equals(fromAccountStr)).findFirst().orElseThrow(
              () -> new FmtException("From Account Not Found")
            );
          Account toAccount = holder.getAccounts().stream()
            .filter(account -> account.getAccountNumber().equals(toAccountStr)).findFirst().orElseThrow(
              () -> new FmtException("To Account Not Found")
            );

          log.info("From Account: {}", fromAccount);
          log.info("From Account: {}", toAccount);

          //=================== Saga orchestration =======================

          // Step 1: Put a withold on toAccount.
          fromAccount.withEventBus(eventBus).withholdMoney(transferAmount)
            .onSuccess(
              event -> {
                log.info("Withhold money: SUCCESS");

                toAccount.withEventBus(eventBus).addPendingDeposit(transferAmount)
                  .onSuccess(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {

                      log.info("Pending deposit: SUCCESS");
                      fromAccount.withdrawMoney(transferAmount)
                        .onSuccess(
                          new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                              log.info("Withdraw Money: SUCCESS");
                              toAccount.confirmDeposit(transferAmount)
                                .onSuccess(new Handler<Void>() {
                                  @Override
                                  public void handle(Void event) {
                                    log.info("Confirm deposit: SUCCESS");
                                    alhPromise.complete();
                                  }
                                });
                            }
                          }
                        ).onFailure(new Handler<Throwable>() {
                        @Override
                        public void handle(Throwable throwable) {
                          log.info("Withdraw Withdraw: FAILED");
                          //throwable.printStackTrace();
                          alhPromise.fail("Withdraw Withdraw: FAILED");
                        }


                      });
                    }
                  }).onFailure(new Handler<Throwable>() {
                  @Override
                  public void handle(Throwable throwable) {
                    log.info("Withhold money: FAILED");
                    alhPromise.fail(throwable.getMessage());
                  }
                });


              }
            )
            .onFailure(new Handler<Throwable>() {
              @Override
              public void handle(Throwable e) {
                log.info("Withhold money: FAILED");
                alhPromise.fail(e.getMessage());
              }
            });
          //==============================================================

        }
      );
    return alhPromise.future();
  }


}
