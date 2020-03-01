package com.chinthakad.samples.fmt.core.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequestDto {
  private String fromAccountNumber;
  private String toAccountNumber;
  private BigDecimal amount;
}
