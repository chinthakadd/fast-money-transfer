package com.chinthakad.samples.fmt.core.model.dto;

import com.chinthakad.samples.fmt.core.model.BaseModel;
import com.chinthakad.samples.fmt.core.model.domain.Transfer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Holds List of transfers created.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferListHolder implements BaseModel {
  private List<Transfer> transfers;

}
