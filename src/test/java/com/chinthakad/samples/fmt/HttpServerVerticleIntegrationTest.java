package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.api.HttpServerVerticle;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import com.chinthakad.samples.fmt.seedwork.codec.AccountListHolderMessageCodec;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class HttpServerVerticleIntegrationTest {

  private static Logger logger = LoggerFactory.getLogger(JdbcClientVerticleIntegrationTest.class);
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
          logger.info("register mock consumer for {} Queue", QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE);
          this.vertx.eventBus()
            .registerDefaultCodec(AccountListHolder.class, new AccountListHolderMessageCodec())
            .consumer(
              QueueConfig.CONFIG_ACCOUNT_SERVICE_QUEUE,
              (Handler<Message<Void>>) reqMessage -> reqMessage.reply(
                new AccountListHolder()
                  .withAccounts(
                    Account.builder().accountNumber("123").build(),
                    Account.builder().accountNumber("234").build()
                  )
              )
            );

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
  }

  @Test
  public void testGetAccounts(VertxTestContext vertxTestContext) {
    client.get(port, "localhost", "/accounts")
      //.as(BodyCodec.string())
      .send(vertxTestContext.succeeding(response -> vertxTestContext.verify(() -> {
        AccountListHolder payload = response.bodyAsJson(AccountListHolder.class);
        assertNotNull(payload);
        assertTrue(!payload.getAccounts().isEmpty());
        assertTrue(payload.getAccounts().size() == 2);
        vertxTestContext.completeNow();
      })));
  }

}
