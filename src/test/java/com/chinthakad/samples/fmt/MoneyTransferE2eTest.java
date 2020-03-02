package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.api.HttpServerVerticle;
import com.chinthakad.samples.fmt.client.JdbcClientVerticle;
import com.chinthakad.samples.fmt.core.model.dto.TransferRequestDto;
import com.chinthakad.samples.fmt.core.service.AccountVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2e Test Cases demonstrating how application behaves against different consumer interactions.
 */
@ExtendWith(VertxExtension.class)
public class MoneyTransferE2eTest {

  private static Logger logger = LoggerFactory.getLogger(MoneyTransferE2eTest.class);
  private Vertx vertx;
  private WebClient client;
  private int port = 8080;

  @BeforeEach
  public void prepare(VertxTestContext testContext) {
    vertx = Vertx.vertx();
    client = WebClient.create(vertx);

    Promise<String> initHttpServer = Promise.promise();
    vertx.deployVerticle(new HttpServerVerticle(), initHttpServer);
    initHttpServer.future()
      .compose(httpServerVerticleId -> {
        Promise<String> dbClientVerticleDeployment = Promise.promise();
        vertx.deployVerticle(new JdbcClientVerticle(), dbClientVerticleDeployment);
        return dbClientVerticleDeployment.future();
      })
      .compose(httpServerVerticleId -> {
        Promise<String> accountServiceDeployment = Promise.promise();
        vertx.deployVerticle(new AccountVerticle(), accountServiceDeployment);
        return accountServiceDeployment.future();
      }).setHandler(
      ar -> {
        testContext.completeNow();
      }
    );
  }

  @Test
  public void testGetAccounts(VertxTestContext vertxTestContext) {
    client.get(port, "localhost", "/accounts")
      .send(vertxTestContext.succeeding(response -> vertxTestContext.verify(() -> {
        logger.info("Response : {}", response.body().toString());
        JsonArray payload = response.bodyAsJsonArray();
        assertTrue(payload.size() == 3, "3 Accounts Defined");
        payload.stream()
          .forEach(
            account -> {
              assertNotNull(account, "Account cannot be null");
              JsonObject accountJson = (JsonObject) account;
              assertNotNull(accountJson.getString("accountNumber"));
              assertNotNull(accountJson.getString("name"));
              assertNotNull(accountJson.getString("currentBalance"));
              assertNotNull(accountJson.getString("availableBalance"));
              assertNotNull(accountJson.getString("lastUpdatedOn"));
            }
          );
        vertxTestContext.completeNow();
      })));
  }

  @Test
  public void transferWithSuccess(VertxTestContext vertxTestContext) {

    client.get(port, "localhost", "/accounts")
      .send(vertxTestContext.succeeding(response -> vertxTestContext.verify(() -> {
          JsonArray payload = response.bodyAsJsonArray();

          JsonObject fromAccount = payload.getJsonObject(0);
          String fromAccountNumber = fromAccount.getString("accountNumber");
          BigDecimal fromAccountCurrentBalance = new BigDecimal(fromAccount.getString("currentBalance"));
          BigDecimal fromAccountAvailableBalance = new BigDecimal(fromAccount.getString("availableBalance"));

          JsonObject toAccount = payload.getJsonObject(1);
          String toAccountNumber = toAccount.getString("accountNumber");
          BigDecimal toAccountCurrentBalance = new BigDecimal(toAccount.getString("currentBalance"));
          BigDecimal toAccountAvailableBalance = new BigDecimal(toAccount.getString("availableBalance"));

          BigDecimal transferAmount = new BigDecimal(30);
          TransferRequestDto transferRequestDto = TransferRequestDto.builder()
            .fromAccountNumber(fromAccountNumber)
            .toAccountNumber(toAccountNumber)
            .amount(transferAmount)
            .build();

          client.post(port, "localhost", "/accounts/transfers")
            .sendJson(transferRequestDto, new Handler<AsyncResult<HttpResponse<Buffer>>>() {
              @Override
              public void handle(AsyncResult<HttpResponse<Buffer>> event) {

                logger.info("Await for Transfer to occur");
                try {
                  vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }

                client.get(port, "localhost", "/accounts")
                  .send(updatedAccounts -> {
                    try {

                      logger.info("=================");
                      logger.info("{}", updatedAccounts.result().bodyAsJsonArray());
                      logger.info("=================");
                    } finally {
                      vertxTestContext.completeNow();
                    }

                    // Check if current balance of fromAccount is updated properly.
                    assertEquals(fromAccountCurrentBalance.add(transferAmount.negate()),
                      new BigDecimal(updatedAccounts.result().bodyAsJsonArray().getJsonObject(0).getString("currentBalance")));

                    // Check if available balance of fromAccount updated properly.
                    assertEquals(fromAccountAvailableBalance.add(transferAmount.negate()),
                      new BigDecimal(updatedAccounts.result().bodyAsJsonArray().getJsonObject(0).getString("availableBalance")));

                    // Check if current balance of fromAccount is updated properly.
                    assertEquals(toAccountCurrentBalance.add(transferAmount),
                      new BigDecimal(updatedAccounts.result().bodyAsJsonArray().getJsonObject(1).getString("currentBalance")));

                    // Check if available balance of fromAccount updated properly.
                    assertEquals(toAccountAvailableBalance.add(transferAmount),
                      new BigDecimal(updatedAccounts.result().bodyAsJsonArray().getJsonObject(1).getString("availableBalance")));

                  });
              }
            });
        }))
      );
  }

  @Test
  public void transferWithInsufficientFunds() {

  }

}
