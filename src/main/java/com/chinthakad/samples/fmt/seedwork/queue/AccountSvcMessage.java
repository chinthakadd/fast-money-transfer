package com.chinthakad.samples.fmt.seedwork.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSvcMessage<T> {

  private ActionType action;
  private T data;
  public enum ActionType {
    LIST, TRANSFER
  }
}
