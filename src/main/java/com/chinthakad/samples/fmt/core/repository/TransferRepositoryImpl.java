package com.chinthakad.samples.fmt.core.repository;

import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public class TransferRepositoryImpl implements TransferRepository {

  private EventBus eventBus;

  public TransferRepositoryImpl(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public Future<TransferListHolder> findAll() {
    Promise<TransferListHolder> alhPromise = Promise.promise();
    eventBus.request(QueueConfig.CONFIG_TRANSFER_CLIENT_JDBC_QUEUE,
      JdbcClientMessage.builder().requestType(JdbcClientMessage.RequestType.SELECT).build(),
      (Handler<AsyncResult<Message<TransferListHolder>>>) event -> {
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
