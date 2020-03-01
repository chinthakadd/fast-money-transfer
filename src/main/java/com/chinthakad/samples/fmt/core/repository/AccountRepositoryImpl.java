package com.chinthakad.samples.fmt.core.repository;

import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import com.chinthakad.samples.fmt.seedwork.queue.AccountJdbcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class AccountRepositoryImpl implements AccountRepository {

  private EventBus eventBus;

  public AccountRepositoryImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public Future<AccountListHolder> findAll() {
    Promise<AccountListHolder> alhPromise = Promise.promise();
    eventBus.request(QueueConfig.CONFIG_ACCOUNT_CLIENT_JDBC_QUEUE,
      AccountJdbcMessage.builder().requestType(AccountJdbcMessage.RequestType.SELECT).build(),
      (Handler<AsyncResult<Message<AccountListHolder>>>) event -> {
        if (event.failed()) {
          event.cause().printStackTrace();
          alhPromise.fail("Unable to get accounts");
        } else {
          alhPromise.complete(event.result().body());
        }
      }
    );
    return alhPromise.future();
  }
}
