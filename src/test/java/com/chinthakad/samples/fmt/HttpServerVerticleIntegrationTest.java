package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.api.HttpServerVerticle;
import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.seedwork.codec.AccountListHolderMessageCodec;
import com.chinthakad.samples.fmt.seedwork.queue.AccountSvcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class HttpServerVerticleIntegrationTest {

  private static Logger logger = LoggerFactory.getLogger(HttpServerVerticleIntegrationTest.class);
  private Vertx vertx;
  private WebClient client;
  private int port = 8080;

  @BeforeEach
  public void prepare(VertxTestContext vertxTestContext) {
    vertx = Vertx.vertx();
    client = WebClient.create(vertx);
    Promise<String> httpServerVerticlePromise = Promise.promise();
    vertx.deployVerticle(new HttpServerVerticle(), httpServerVerticlePromise);
    httpServerVerticlePromise
      .future()
      .setHandler(
        ar -> {
          logger.info("preparation successfully");
          assertTrue(ar.succeeded(), "verticle must be deployed, client mock queue must be ready");
          // TODO: Use a better assertion library.
          boolean serverStarted = true;
          try {
            vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            serverStarted = false;
          }
          vertxTestContext.completeNow();
          assertTrue(serverStarted, "server must be started");
        }
      );

    logger.info("register mock consumer for {} Queue", QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE);
    this.vertx.eventBus()
      .registerDefaultCodec(AccountListHolder.class, new AccountListHolderMessageCodec())
      .consumer(
        QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE,
        (Handler<Message<AccountSvcMessage>>) reqMessage -> reqMessage.reply(
          new AccountListHolder()
            .withAccounts(
              Account.builder().accountNumber("123").build(),
              Account.builder().accountNumber("234").build()
            )
        )
      );
  }

  @Test
  public void testGetAccounts(VertxTestContext vertxTestContext) {
    client.get(port, "localhost", "/accounts")
      .send(vertxTestContext.succeeding(response -> vertxTestContext.verify(() -> {
        System.out.println(response.body().toString());
//        AccountListHolder payload = response.bodyAsJson(AccountListHolder.class);
//        assertNotNull(payload);
//        assertTrue(!payload.getAccounts().isEmpty());
//        assertTrue(payload.getAccounts().size() == 2);
        vertxTestContext.completeNow();
      })));
  }

}
