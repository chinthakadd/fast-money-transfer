package com.chinthakad.samples.fmt.core.model.domain;

import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Represents the Transfer domain. It has one-to-one relationship with {@link Account} in
 * {@link #toAccount}  and {@link #fromAccount}.
 */
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transfer {

  private long id;
  private Account fromAccount;
  private Account toAccount;
  private BigDecimal amount;
  private TransferStatus transferStatus = TransferStatus.PENDING;

  private Instant lastUpdatedTimestamp;

  @JsonIgnore
  private int version;
  @JsonIgnore
  private EventBus eventBus;


  public static Transfer fromJsonArray(JsonArray jsonArray) {
    return Transfer.builder()
      .id(jsonArray.getInteger(0))
      .fromAccount(Account.builder().id(jsonArray.getInteger(1)).build())
      .toAccount(Account.builder().id(jsonArray.getInteger(2)).build())
      .amount(new BigDecimal(jsonArray.getDouble(3)).setScale(2, RoundingMode.HALF_EVEN))
      .transferStatus(TransferStatus.valueOf(jsonArray.getString(4)))
      .lastUpdatedTimestamp(jsonArray.getInstant(5))
      .version(jsonArray.getInteger(6))
      .build();
  }

  public Transfer withEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    return this;
  }

  public Future<Void> initiateTransfer() {

    Promise<Void> promise = Promise.promise();
    log.info("From Account: {}", fromAccount);
    log.info("To Account: {}", toAccount);

    // Persist transfer first.
    Promise<Void> persistPromise = Promise.promise();
    this.save(persistPromise);

    //=================== Money Transfer Orchestration =======================
    // Step 1: Put a withold on fromAccount.
    fromAccount.withEventBus(eventBus).withholdMoney(amount)
      .onSuccess(
        event -> {
          log.info("Withhold money: SUCCESS");

          // Step 2: Add a pending deposit to toAccount.
          toAccount.withEventBus(eventBus).addPendingDeposit(amount)
            .onSuccess(event1 -> {
              log.info("Pending deposit: SUCCESS");

              // Step 3: Withdraw money from fromAccount.
              fromAccount.withdrawMoney(amount)
                .onSuccess(
                  event11 -> {
                    log.info("Withdraw Money: SUCCESS");

                    // Step 4: Make funds available.
                    toAccount.makeFundsAvailable(amount)
                      .onSuccess(event111 -> {
                        log.info("Confirm deposit: SUCCESS");
                        promise.complete();
                      });
                    // TODO: On failure: Need to retry this.
                  }
                ).onFailure(throwable -> {
                log.info("Withdraw Withdraw: FAILED");
                // Compensate:
                // Remove pendingDeposit from toAccount
                // Make funds available.
                toAccount.withdrawMoney(amount)
                  .onSuccess(
                    ar -> {
                      fromAccount.makeFundsAvailable(amount)
                        .onSuccess(
                          ar2 -> promise.fail("Withdraw: FAILED")
                        );
                    }
                  );
              });

            }).onFailure(throwable -> {
            log.info("Withhold money: FAILED");
            // Compensate: Remove Withhold from toAccount,
            // => making funds available
            // NOTE:
            // This can also fail due to concurrency.
            // It can be solved by a RETRY operation since its only making funds added.
            fromAccount.makeFundsAvailable(amount)
              .setHandler(ar -> promise.fail(throwable.getMessage()));
          });
        }
      )
      .onFailure(e -> {
        log.info("Withhold money: FAILED");
        promise.fail(e.getMessage());
      });
    //======================================================================
    return promise.future();
  }


  public void save(Promise<Void> wmp) {
    this.eventBus.request(
      QueueConfig.CONFIG_TRANSFER_CLIENT_JDBC_QUEUE,
      JdbcClientMessage.builder().requestType(JdbcClientMessage.RequestType.SAVE).data(this).build(),
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

  public void update(Promise<Void> wmp) {
    this.eventBus.request(
      QueueConfig.CONFIG_TRANSFER_CLIENT_JDBC_QUEUE,
      JdbcClientMessage.builder().requestType(JdbcClientMessage.RequestType.UPDATE).data(this).build(),
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

  public enum TransferStatus {
    /**
     * Transfer is pending
     */
    PENDING,
    /**
     * Transfer completed
     */
    COMPLETED,
    /**
     * failed putting a withhold in {@link #fromAccount}
     */
    FAILED_TO_WITHHELD,
    /**
     * failed withdrawing money from {@link #fromAccount}
     */
    FAILED_TO_WITHDRAW,
    /**
     * failed adding a pending deposit {@link #toAccount}
     */
    FAILED_TO_ADD_PENDING_DEPOSIT,

    /**
     * Failed to make funds available to {@link #toAccount}
     */
    FAILED_TO_MAKE_FUNDS_AVAILABLE
  }
}
