package com.chinthakad.samples.fmt.client;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.Transfer;
import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcClientVerticle extends AbstractVerticle {

  private AccountJdbcClient accountJdbcClient;
  private TransferJdbcClient transferJdbcClient;

  @Override
  public void start(Promise<Void> startPromise) {
    initDatabase().onComplete(
      ar -> {
        if (ar.succeeded()) {
          vertx.eventBus().consumer(QueueConfig.CONFIG_ACCOUNT_CLIENT_JDBC_QUEUE, this::forAccount);
          vertx.eventBus().consumer(QueueConfig.CONFIG_TRANSFER_CLIENT_JDBC_QUEUE, this::forTransfer);
          log.info("=== VERTICLE DEPLOYED : {}", this.getClass().getSimpleName());
          startPromise.complete();
        } else {
          startPromise.fail("DB Initialization failed.");
        }
      });
  }

  private Future<Void> initDatabase() {
    JDBCClient jdbcClient = JDBCClient.createShared(vertx,
      new JsonObject()
        .put("url", "jdbc:h2:mem:test-db;DB_CLOSE_ON_EXIT=FALSE")
        .put("driver_class", "org.h2.Driver")
        .put("user", "sa")
        .put("password", "")
        .put("max_pool_size", 30));

    accountJdbcClient = new AccountJdbcClient(jdbcClient);
    transferJdbcClient = new TransferJdbcClient(jdbcClient);
    return accountJdbcClient.initTable().compose(aVoid -> transferJdbcClient.initTable());
  }

  private void forAccount(Message<JdbcClientMessage> message) {

    JdbcClientMessage<Account> jdbcClientMessage = message.body();
    if (jdbcClientMessage.getRequestType() == JdbcClientMessage.RequestType.SELECT) {
      // TODO: Handle error scenarios.
      this.accountJdbcClient.getAccounts()
        .onSuccess(accountListHolder -> {
            message.reply(accountListHolder);
          }
        );
    }

    if (jdbcClientMessage.getRequestType() == JdbcClientMessage.RequestType.UPDATE) {
      // TODO: Handle error scenarios.
      this.accountJdbcClient.updateAccount(jdbcClientMessage.getData())
        .onSuccess(accountListHolder -> {
            log.info("=== SUCCESS: JDBC CLIENT VERTICLE");
            // TODO: Just replying with a boolean for now.
            message.reply(true);
          }
        ).onFailure(event -> message.reply(false)
      );
    }
  }

  private void forTransfer(Message<JdbcClientMessage> message) {

    JdbcClientMessage<Transfer> jdbcClientMessage = message.body();
    if (jdbcClientMessage.getRequestType() == JdbcClientMessage.RequestType.SELECT) {
      // TODO: Handle error scenarios.
      this.transferJdbcClient.getTransfers()
        .onSuccess(tlh -> {
            message.reply(tlh);
          }
        );
    }

    if (jdbcClientMessage.getRequestType() == JdbcClientMessage.RequestType.UPDATE) {
      // TODO: Handle error scenarios.
      this.transferJdbcClient.updateTransfer(jdbcClientMessage.getData())
        .onSuccess(accountListHolder -> {
            log.info("=== SUCCESS: JDBC CLIENT VERTICLE");
            // TODO: Just replying with a boolean for now.
            message.reply(true);
          }
        ).onFailure(event -> message.reply(false)
      );
    }

    if (jdbcClientMessage.getRequestType() == JdbcClientMessage.RequestType.SAVE) {
      // TODO: Handle error scenarios.
      this.transferJdbcClient.saveTransfer(jdbcClientMessage.getData())
        .onSuccess(accountListHolder -> {
            log.info("=== SUCCESS: JDBC CLIENT VERTICLE");
            message.reply(true);
          }
        ).onFailure(event -> message.reply(false)
      );
    }

  }
}
