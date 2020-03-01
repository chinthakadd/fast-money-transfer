package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.client.JdbcClientVerticle;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import com.chinthakad.samples.fmt.core.model.domain.AccountListHolder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class JdbcClientVerticleIntegrationTest {

  private static Logger logger = LoggerFactory.getLogger(JdbcClientVerticleIntegrationTest.class);
  private Vertx vertx;

  @BeforeEach
  public void prepare(VertxTestContext testContext) {
    logger.info("preparation of database");
    this.vertx = Vertx.vertx();
    this.vertx.deployVerticle(
      new JdbcClientVerticle(),
      deploymentId -> {
        logger.info("database initialized");
        testContext.completeNow();
      }
    );
  }

  @Test
  public void testGetAccounts(VertxTestContext testContext) {
    logger.info("start test for getAccounts()");
    this.vertx.eventBus().request(
      QueueConfig.CONFIG_ACCOUNT_CLIENT_JDBC_QUEUE,
      new JsonObject(),
      (Handler<AsyncResult<Message<AccountListHolder>>>) event -> {
        assertTrue(event.succeeded(), "response event must succeed");
        assertFalse(event.failed(), "response event must not fail");

        Message<AccountListHolder> message = event.result();
        AccountListHolder alh = message.body();

        assertNotNull(alh);
        assertFalse(alh.getAccounts().isEmpty());

        alh.getAccounts()
          .forEach(
            account -> {
              assertTrue(account.getId() != 0);
              assertNotNull(account.getAccountNumber());
              assertNotNull(account.getAvailableBalance());
              assertNotNull(account.getCurrentBalance());
              assertNotNull(account.getDisplayName());
            }
          );
      }
    );
    testContext.completeNow();
  }
}
