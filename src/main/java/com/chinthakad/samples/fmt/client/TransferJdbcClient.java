package com.chinthakad.samples.fmt.client;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.Transfer;
import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class TransferJdbcClient {

  /**
   * TODO: There is a foreign-key relationship with Account Table that needs to be implemented.
   */
  private static final String SQL_CREATE_TABLE_TRANSFER =
    "CREATE TABLE transfers(" +
      "id IDENTITY," +
      "from_account_id INT NOT NULL, " +
      "to_account_id INT NOT NULL, " +
      "transfer_amount DECIMAL NOT NULL, " +
      "status VARCHAR(50) NOT NULL DEFAULT 'PENDING'," +
      "last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
      "version INT NOT NULL DEFAULT 0)";

  private static final String SQL_INSERT_TRANSFER =
    "insert into transfers(from_account_id, to_account_id, transfer_amount, last_updated_date) " +
      "values (?, ?, ?, CURRENT_TIMESTAMP)";

  private static final String SQL_SELECT_TRANSFER = "select * from transfers";

  private static final String SQL_UPDATE_TRANSFER =
    "UPDATE transfers SET " +
      "status = ?, last_updated_date = CURRENT_TIMESTAMP, version=? " +
      "WHERE id=? and version = ?";


  private JDBCClient jdbcClient;

  public TransferJdbcClient(JDBCClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Future<Void> initTable() {
    Promise<Void> promise = Promise.promise();
    jdbcClient.getConnection(
      ar -> {
        if (ar.succeeded()) {
          SQLConnection sqlConn = ar.result();
          ar.result().execute(SQL_CREATE_TABLE_TRANSFER,
            create -> {
              if (create.succeeded()) {
                log.info("Create Table Succeeded for Transfers");
                promise.complete();
              } else {
                log.info("Create Table Failed");
                create.cause().printStackTrace();
                promise.fail("Create Table Failed:");
              }
            }
          );
        }
      });
    return promise.future();
  }

  public Future<TransferListHolder> getTransfers() {
    Promise<TransferListHolder> tlhPromise = Promise.promise();
    this.jdbcClient.getConnection(
      arSql -> {
        if (arSql.succeeded()) {
          arSql.result().query(SQL_SELECT_TRANSFER,
            res -> {
              if (res.succeeded()) {
                Optional<TransferListHolder> accountListHolderOpt = res.result().getResults()
                  .stream()
                  .map(jsonArray -> new TransferListHolder(Arrays.asList(Transfer.fromJsonArray(jsonArray))))
                  .reduce((t1, t2) -> new TransferListHolder(Arrays.asList(t1.getTransfers().get(0), t2.getTransfers().get(0))));
                tlhPromise.complete(accountListHolderOpt.orElse(new TransferListHolder()));
              }
            });
        } else {
          log.error("Unable to get accounts");
          tlhPromise.fail("Failed to acquire accounts");
        }
      }
    );
    return tlhPromise.future();
  }

  public Future<Void> saveTransfer(Transfer transfer) {
    Promise<Void> savePromise = Promise.promise();
    log.info("Save transfer: {}", transfer);
    this.jdbcClient.getConnection(
      arSql -> {
        if (arSql.succeeded()) {
          arSql.result().updateWithParams(

            SQL_INSERT_TRANSFER,

            // Update Object
            new JsonArray().add(transfer.getFromAccount().getId())
              .add(transfer.getToAccount().getId())
              .add(transfer.getAmount().toPlainString()),

            // On Success
            result -> {
              if (result.succeeded()) {
                log.info("saved ===> {}", result.result().getUpdated());
                if (result.result().getUpdated() != 1) {
                  log.error("Save failed due to a concurrency failure");
                  savePromise.fail("Save update due to optimistic concurrency");
                } else {
                  log.info("save transfer successfully");
                  savePromise.complete();
                }
              } else {
                result.cause().printStackTrace();
                savePromise.fail("Failed to save transfer");
              }
            }
          );
        } else {
          log.error("Transfer save failed for: {}", transfer.getId());
          savePromise.fail("Save to update transfer");
        }
      }
    );
    return savePromise.future();
  }

  public Future<Void> updateTransfer(Transfer transfer) {
    Promise<Void> updatePromise = Promise.promise();
    log.info("Update for transfer: {}", transfer);
    this.jdbcClient.getConnection(
      arSql -> {
        if (arSql.succeeded()) {
          arSql.result().updateWithParams(

            SQL_UPDATE_TRANSFER,

            // Update Object
            new JsonArray().add(transfer.getTransferStatus().name())
              .add(transfer.getVersion() + 1)
              .add(transfer.getId())
              .add(transfer.getVersion()),

            // On Success
            result -> {
              if (result.succeeded()) {
                log.info("updated ===> {}", result.result().getUpdated());
                if (result.result().getUpdated() != 1) {
                  log.error("Update failed due to a concurrency failure");
                  updatePromise.fail("Failed update due to optimistic concurrency");
                } else {
                  log.info("updated transfer successfully");
                  updatePromise.complete();
                }
              } else {
                result.cause().printStackTrace();
                updatePromise.fail("Failed to update transfer");
              }
            }
          );
        } else {
          log.error("Transfer update failed for: {}", transfer.getId());
          updatePromise.fail("Failed to update transfer");
        }
      }
    );
    return updatePromise.future();
  }
}
