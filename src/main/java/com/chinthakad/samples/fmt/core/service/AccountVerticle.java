package com.chinthakad.samples.fmt.core.service;

import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.seedwork.codec.AccountCommandCodec;
import com.chinthakad.samples.fmt.seedwork.codec.AccountDataRequestCodec;
import com.chinthakad.samples.fmt.seedwork.codec.AccountListHolderMessageCodec;
import com.chinthakad.samples.fmt.seedwork.queue.AccountJdbcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountVerticle extends AbstractVerticle {

  private AccountService accountService;

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.eventBus()
      .registerDefaultCodec(AccountListHolder.class, new AccountListHolderMessageCodec())
      .registerDefaultCodec(AccountJdbcMessage.class, new AccountDataRequestCodec())
      .registerDefaultCodec(AccountSvcMessage.class, new AccountCommandCodec())
      .consumer(QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE, this::onMessage);
    this.accountService = new AccountServiceImpl(vertx.eventBus());
    log.info("=== VERTICLE DEPLOYED : {}", this.getClass().getSimpleName());
    startPromise.complete();
  }

  private void onMessage(Message<AccountSvcMessage> message) {
    AccountSvcMessage<TransferRequestDto> accountSvcMessage = message.body();
    if (accountSvcMessage.getAction() == AccountSvcMessage.ActionType.LIST) {
      this.accountService.getAccounts().onSuccess(alh -> message.reply(alh))
        .onFailure(throwable -> {//TODO: Handle this properly.
            throwable.printStackTrace();
          }
        );
    }
    if (accountSvcMessage.getAction() == AccountSvcMessage.ActionType.TRANSFER) {
      this.accountService.transferMoney(accountSvcMessage.getData())
        .onSuccess(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            log.info("Transfer Money: COMPLETED");
            message.reply(Boolean.TRUE);
          }
        })
        .onFailure(throwable -> {
            //TODO: Handle this properly.
            // Add response structure.
            throwable.printStackTrace();
            message.reply(Boolean.FALSE);
          }
        );
    }
  }
}

