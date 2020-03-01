package com.chinthakad.samples.fmt.seedwork.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountJdbcMessage<T> {
  private RequestType requestType;
  private T data;

  public enum RequestType {
    SELECT, UPDATE
  }
}
