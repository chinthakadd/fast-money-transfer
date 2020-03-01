package com.chinthakad.samples.fmt;

import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.exception.InsufficientFundsException;
import com.chinthakad.samples.fmt.seedwork.codec.JdbcMessageCodec;
import com.chinthakad.samples.fmt.seedwork.codec.AccountListHolderMessageCodec;
import com.chinthakad.samples.fmt.seedwork.queue.JdbcClientMessage;
import com.chinthakad.samples.fmt.seedwork.queue.QueueConfig;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class AccountUnitTest {

  private Vertx vertx;

  @BeforeEach
  public void prepare() {
    this.vertx = Vertx.vertx();
    this.vertx.eventBus().registerDefaultCodec(JdbcClientMessage.class, new JdbcMessageCodec());
    this.vertx.eventBus()
      .registerDefaultCodec(AccountListHolder.class, new AccountListHolderMessageCodec())
      .consumer(
        QueueConfig.CONFIG_ACCOUNT_CLIENT_JDBC_QUEUE,
        reqMessage -> reqMessage.reply(true)
      );
  }

  @Test
  public void testCheckForSufficiency() {
    Account account = Account.builder().availableBalance(BigDecimal.ZERO).build();
    assertThrows(InsufficientFundsException.class, () -> account.checkForSufficientFunds(new BigDecimal(100.12)));
  }

  @Test
  public void testWithdrawMoney() {
    Account account = Account.builder().currentBalance(BigDecimal.TEN).build();
    account.withEventBus(this.vertx.eventBus()).withdrawMoney(BigDecimal.TEN)
      .setHandler(event -> assertTrue(account.getCurrentBalance().equals(BigDecimal.ZERO)));
  }

  @Test
  public void testWithholdMoney() {
    Account account = Account.builder().availableBalance(new BigDecimal(20)).build();
    account.withEventBus(this.vertx.eventBus()).withholdMoney(BigDecimal.TEN)
      .setHandler(event -> assertTrue(account.getAvailableBalance().equals(BigDecimal.TEN)));
  }

  @Test
  public void testMakeFundsAvailable() {
    Account account = Account.builder().availableBalance(BigDecimal.TEN).build();
    account.withEventBus(this.vertx.eventBus()).makeFundsAvailable(BigDecimal.TEN)
      .setHandler(event -> assertTrue(account.getAvailableBalance().equals(new BigDecimal(20))));
  }
}
