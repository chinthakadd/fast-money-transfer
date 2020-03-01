package com.chinthakad.samples.fmt.core.service;

import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.seedwork.codec.AccountListHolderMessageCodec;
import com.chinthakad.samples.fmt.seedwork.codec.AccountSvcMessageCodec;
import com.chinthakad.samples.fmt.seedwork.codec.JdbcMessageCodec;
import com.chinthakad.samples.fmt.seedwork.codec.TransferListHolderMessageCodec;
import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;
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
      .registerDefaultCodec(TransferListHolder.class, new TransferListHolderMessageCodec())
      .registerDefaultCodec(JdbcClientMessage.class, new JdbcMessageCodec())
      .registerDefaultCodec(AccountSvcMessage.class, new AccountSvcMessageCodec())
      .consumer(QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE, this::onMessage);
    this.accountService = new AccountServiceImpl(vertx.eventBus());
    log.info("=== VERTICLE DEPLOYED : {}", this.getClass().getSimpleName());
    startPromise.complete();
  }

  private void onMessage(Message<AccountSvcMessage> message) {
    AccountSvcMessage accountSvcMessage = message.body();
    if (accountSvcMessage.getAction() == AccountSvcMessage.ActionType.LIST_ACCOUNTS) {
      this.accountService.getAccounts().onSuccess(alh -> message.reply(alh))
        .onFailure(throwable -> {//TODO: Handle this properly.
            throwable.printStackTrace();
          }
        );
    }
    if (accountSvcMessage.getAction() == AccountSvcMessage.ActionType.LIST_TRANSFERS) {
      this.accountService.getTransfers().onSuccess(alh -> message.reply(alh))
        .onFailure(throwable -> {//TODO: Handle this properly.
            throwable.printStackTrace();
          }
        );
    }
    if (accountSvcMessage.getAction() == AccountSvcMessage.ActionType.TRANSFER) {
      this.accountService.transferMoney((TransferRequestDto) accountSvcMessage.getData())
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
