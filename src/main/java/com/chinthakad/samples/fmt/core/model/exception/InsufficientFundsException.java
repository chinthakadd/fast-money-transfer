package com.chinthakad.samples.fmt.core.model.exception;

import com.chinthakad.samples.fmt.seedwork.FmtException;

import java.math.BigDecimal;

public class InsufficientFundsException extends FmtException {

  public InsufficientFundsException(long accountId, BigDecimal availableBalance, BigDecimal withdrawAmount) {
    super(String.format("Insufficient Funds in [account=%s] => [available balance=%s] > [withdrawAmount=%s] ",
      accountId, availableBalance, withdrawAmount));
  }
}
