package com.chinthakad.samples.fmt.core.repository;

import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import com.chinthakad.samples.fmt.core.model.dto.TransferListHolder;
import io.vertx.core.Future;

public interface TransferRepository {
  Future<TransferListHolder> findAll();
}
