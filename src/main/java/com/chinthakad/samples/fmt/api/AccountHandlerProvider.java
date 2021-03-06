package com.chinthakad.samples.fmt.api;

import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountHandlerProvider {

  private Vertx vertx;

  public AccountHandlerProvider(Vertx vertx) {
    this.vertx = vertx;
  }

  public Handler<RoutingContext> getAccounts() {
    return routingContext -> {
      log.info("GET /accounts");
      vertx.eventBus().request(QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE,
        AccountSvcMessage.builder().action(AccountSvcMessage.ActionType.LIST_ACCOUNTS).build(),
        (Handler<AsyncResult<Message<AccountListHolder>>>) reply -> {
          if (reply.succeeded()) {
            JsonArray accountAry = new JsonArray();
            reply.result().body().getAccounts()
              .stream().forEach(
              account -> accountAry.add(
                new JsonObject().put("accountNumber", account.getAccountNumber())
                  .put("name", account.getDisplayName())
                  .put("availableBalance", account.getAvailableBalance().toPlainString())
                  .put("currentBalance", account.getCurrentBalance().toPlainString())
                  .put("lastUpdatedOn", account.getLastUpdatedTimestamp())
              )
            );
            routingContext.response().end(Json.encodePrettily(accountAry));
          } else {
            // TODO: Handle error.
            // TODO: Lets adopt JSEND.
            reply.cause().printStackTrace();
            routingContext.fail(500);
          }
        });
    };
  }

  public Handler<RoutingContext> transferMoney() {
    return routingContext -> {
      log.info("POST /accounts/transfers");
      // TODO: Dont use TransferRequest. Use a presentation DTO.

      routingContext.request().bodyHandler(reqBuf -> {
        TransferRequestDto transferRequest = Json.decodeValue(reqBuf, TransferRequestDto.class);
        log.info("received request: {}", transferRequest);

        vertx.eventBus()
          .request(
            QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE,
            AccountSvcMessage.builder().action(AccountSvcMessage.ActionType.TRANSFER).data(transferRequest).build(),
            // TODO: Payload can be made better.
            (Handler<AsyncResult<Message<Boolean>>>) reply -> {
              if (reply.succeeded()) {
                Boolean response = reply.result().body();
                log.info("RESPONSE: {}", response);
                if (response) {
                  routingContext.response().setStatusCode(202).end();
                } else {
                  routingContext.fail(500);
                }
              } else {
                reply.cause().printStackTrace();
                routingContext.fail(500);
              }
            });
      });
    };
  }

  public Handler<RoutingContext> getTransfers() {
    return routingContext -> {
      log.info("GET /accounts/transfers");
      vertx.eventBus().request(QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE,
        AccountSvcMessage.builder().action(AccountSvcMessage.ActionType.LIST_TRANSFERS).build(),
        (Handler<AsyncResult<Message<TransferListHolder>>>) reply -> {
          if (reply.succeeded()) {
            JsonArray transfersAry = new JsonArray();
            reply.result().body().getTransfers()
              .stream().forEach(
              transfer -> transfersAry.add(
                new JsonObject().put("id", transfer.getId())
                  .put("fromAccountNumber", transfer.getFromAccount().getAccountNumber())
                  .put("toAccountNumber", transfer.getToAccount().getAccountNumber())
                  .put("amount", transfer.getAmount().toPlainString())
                  .put("status", transfer.getTransferStatus().name())
                  .put("lastUpdatedOn", transfer.getLastUpdatedTimestamp())
              )
            );
            routingContext.response().end(Json.encodePrettily(transfersAry));
          } else {
            // TODO: Handle error.
            // TODO: Lets adopt JSEND.
            reply.cause().printStackTrace();
            routingContext.fail(500);
          }
        });
    };
  }
}
