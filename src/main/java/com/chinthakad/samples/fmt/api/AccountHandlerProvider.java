package com.chinthakad.samples.fmt.api;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
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
        AccountSvcMessage.builder().action(AccountSvcMessage.ActionType.LIST).build(),
        (Handler<AsyncResult<Message<AccountListHolder>>>) reply -> {
          if (reply.succeeded()) {
            routingContext.response().end(Json.encodePrettily(reply.result().body()));
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
}
