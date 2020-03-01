package com.chinthakad.samples.fmt.client;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * JDBC Client Implementation for Account.
 */
@Slf4j
public class AccountJdbcClient {

  private static final String SQL_CREATE_TABLE_ACCOUNTS =
    "CREATE TABLE accounts(" +
      "id IDENTITY," +
      "account_number VARCHAR(15) NOT NULL, " +
      "display_name VARCHAR(50) NOT NULL, " +
      "current_balance DECIMAL NOT NULL, " +
      "available_balance DECIMAL NOT NULL, " +
      "last_updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
      "version INT NOT NULL DEFAULT 0)";

  private static final String SQL_INSERT_ACCOUNT =
    "insert into accounts(account_number, display_name, current_balance, available_balance) values (?, ?, ?, ?)";

  private static final String SQL_SELECT_ACCOUNTS = "select * from accounts";

  private static final String SQL_UPDATE_ACCOUNT =
    "UPDATE accounts SET " +
      "current_balance = ?," +
      "available_balance = ?, last_updated_date = CURRENT_TIMESTAMP, version=? " +
      "WHERE account_number=? and version = ?";

  private JDBCClient jdbcClient;

  public AccountJdbcClient(JDBCClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public Future<Void> initTable() {
    Promise<Void> promise = Promise.promise();
    jdbcClient.getConnection(
      ar -> {
        if (ar.succeeded()) {
          SQLConnection sqlConn = ar.result();
          ar.result().execute(SQL_CREATE_TABLE_ACCOUNTS,
            create -> {
              if (create.succeeded()) {
                log.info("Create Table Succeeded for Accounts");
                loadAccounts(sqlConn);
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

  private void loadAccounts(SQLConnection sqlConn) {
    // TODO: We can do a batch here.
    getInitSampleAccounts()
      .stream()
      .map(account -> new JsonArray()
        .add(account.getAccountNumber())
        .add(account.getDisplayName())
        .add(account.getCurrentBalance().toPlainString())
        .add(account.getAvailableBalance().toPlainString()))
      .forEach(new Consumer<>() {
        @Override
        public void accept(JsonArray jr) {
          sqlConn.updateWithParams(SQL_INSERT_ACCOUNT, jr, event -> log.info(event.result().toString()));
        }
      });
  }

  private List<Account> getInitSampleAccounts() {
    return Arrays.asList(
      Account.builder().accountNumber("1234")
        .availableBalance(BigDecimal.TEN).currentBalance(BigDecimal.TEN)
        .displayName("test account 1").build(),

      Account.builder().accountNumber("5678")
        .availableBalance(BigDecimal.TEN).currentBalance(BigDecimal.TEN)
        .displayName("test account 2").build(),


      Account.builder().accountNumber("6789")
        .availableBalance(BigDecimal.TEN).currentBalance(BigDecimal.TEN)
        .displayName("test account 4").build()
    );
  }


  /**
   * TODO: Add an interface.
   *
   * @return
   */
  public Future<AccountListHolder> getAccounts() {
    Promise<AccountListHolder> accountListHolderPromise = Promise.promise();
    this.jdbcClient.getConnection(
      arSql -> {
        if (arSql.succeeded()) {
          arSql.result().query(SQL_SELECT_ACCOUNTS,
            res -> {
              if (res.succeeded()) {
                Optional<AccountListHolder> accountListHolderOpt = res.result().getResults()
                  .stream()
                  .map(jsonArray -> new AccountListHolder().withAccounts(Account.fromJsonArray(jsonArray)))
                  .reduce((al1, al2) -> new AccountListHolder().withAccounts(al1.getAccounts()).withAccounts(al2.getAccounts()));
                accountListHolderPromise.complete(accountListHolderOpt.orElse(new AccountListHolder()));
              }
            });
        } else {
          log.error("Unable to get accounts");
          accountListHolderPromise.fail("Failed to acquire accounts");
        }
      }
    );
    return accountListHolderPromise.future();
  }

  public Future<Void> updateAccount(Account account) {
    Promise<Void> updatePromise = Promise.promise();
    log.info("Updated acount: {}", account);
    this.jdbcClient.getConnection(
      arSql -> {
        if (arSql.succeeded()) {
          arSql.result().updateWithParams(
            SQL_UPDATE_ACCOUNT,
            new JsonArray().add(account.getCurrentBalance().toPlainString())
              .add(account.getAvailableBalance().toPlainString())
              .add(account.getVersion() + 1)
              .add(account.getAccountNumber())
              .add(account.getVersion()),
            result -> {
              if (result.succeeded()) {
                log.info("updated ===> {}", result.result().getUpdated());
                if (result.result().getUpdated() != 1) {
                  log.error("Update failed due to a concurrency failure");
                  updatePromise.fail("Failed update due to optimistic concurrency");
                } else {
                  log.info("updated account successfully");
                  updatePromise.complete();
                }
              } else {
                result.cause().printStackTrace();
                updatePromise.fail("Failed to update account");
              }
            }
          );
        } else {
          log.error("Account update failed for: {}", account.getAccountNumber());
          updatePromise.fail("Failed to update account");
        }
      }
    );
    return updatePromise.future();
  }
}
