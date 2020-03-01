package com.chinthakad.samples.fmt.core.model.dto;

import com.chinthakad.samples.fmt.core.model.BaseModel;
import com.chinthakad.samples.fmt.core.model.domain.Account;
import com.chinthakad.samples.fmt.core.model.domain.Transfer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Holds List of transfers created.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferListHolder implements BaseModel {
  private List<Transfer> transfers;


  public TransferListHolder withTransfers(List<Transfer> transfersArg) {
    this.transfers.addAll(transfersArg);
    return this;
  }

  public TransferListHolder withTransfers(Transfer... transfersArg) {
    this.transfers.addAll(Arrays.asList(transfersArg));
    return this;
  }
}
