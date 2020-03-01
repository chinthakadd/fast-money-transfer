package com.chinthakad.samples.fmt.core.model.domain;

import com.chinthakad.samples.fmt.core.model.exception.InsufficientFundsException;
import com.chinthakad.samples.fmt.seedwork.queue.AccountJdbcMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Account Domain model providing attributing to a real world account and provide
 * business functionality such as, checkBalance, debit and credit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {

  private EventBus eventBus;

  private long id;
  private String accountNumber;
  private String displayName;
  private BigDecimal currentBalance;
  private BigDecimal availableBalance;
  private Instant lastUpdatedTimestamp;
  private int version;

  public static Account fromJsonArray(JsonArray jsonArray) {
    return Account.builder()
      .id(jsonArray.getInteger(0))
      .accountNumber(jsonArray.getString(1))
      .displayName(jsonArray.getString(2))
      .availableBalance(new BigDecimal(jsonArray.getDouble(3)).setScale(2, RoundingMode.HALF_EVEN))
      .currentBalance(new BigDecimal(jsonArray.getDouble(4)).setScale(2, RoundingMode.HALF_EVEN))
      .lastUpdatedTimestamp(jsonArray.getInstant(5))
      .version(jsonArray.getInteger(6))
      // TODO: Last Updated Date?
      .build();
  }

  public Account withEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  public void checkForSufficiency(BigDecimal creditAmount) {
    if (availableBalance.compareTo(creditAmount) < 1) {
      throw new InsufficientFundsException(this.id, this.availableBalance, creditAmount);
    }
  }

  public Future<Void> withholdMoney(BigDecimal amount) {
    checkForSufficiency(amount);
    Promise<Void> promise = Promise.promise();
    this.availableBalance = this.availableBalance.add(amount.negate());
    this.persist(promise);
    return promise.future();
  }

  public Future<Void> withdrawMoney(BigDecimal amount) {
    Promise<Void> promise = Promise.promise();
    this.currentBalance = this.currentBalance.add(amount.negate());
    this.persist(promise);
    return promise.future();
  }

  public Future<Void> addPendingDeposit(BigDecimal amount) {
    Promise<Void> promise = Promise.promise();
    this.currentBalance = this.currentBalance.add(amount);
    this.persist(promise);
    return promise.future();
  }

  public Future<Void> confirmDeposit(BigDecimal amount) {
    Promise<Void> promise = Promise.promise();
    this.availableBalance = this.availableBalance.add(amount);
    this.persist(promise);
    return promise.future();
  }

  public void persist(Promise<Void> wmp) {
    this.eventBus.request(
      QueueConfig.CONFIG_ACCOUNT_CLIENT_JDBC_QUEUE,
      AccountJdbcMessage.builder().requestType(AccountJdbcMessage.RequestType.UPDATE).data(this).build(),
      (Handler<AsyncResult<Message<Boolean>>>) ar -> {
        // TODO: If persistence failed,
        // 1. it could be DB Failure
        // 2. it could be due to optimistic concurrency.
        if (ar.succeeded()) {
          if (!ar.result().body()) {
            wmp.fail("Persistence failed due to a business reason");
            return;
          }
          this.version = version + 1;
          wmp.complete();
        } else {
          wmp.fail("Persistence failed due to a technical reason");
        }
      }
    );
  }

  public static void main(String[] args) {
    BigDecimal bg1 = BigDecimal.TEN;
    System.out.println(bg1.add(BigDecimal.ONE));
  }
}
