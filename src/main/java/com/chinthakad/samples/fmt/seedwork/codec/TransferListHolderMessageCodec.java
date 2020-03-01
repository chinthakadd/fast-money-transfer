package com.chinthakad.samples.fmt.seedwork.codec;

import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;

public class TransferListHolderMessageCodec extends LocalOnlyMessageCodec<TransferListHolder, TransferListHolder> {


  @Override
  public TransferListHolder transform(TransferListHolder transfers) {
    return transfers;
  }

  @Override
  public String name() {
    return "TransferListCodec";
  }

}
