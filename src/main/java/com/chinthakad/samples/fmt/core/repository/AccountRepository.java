package com.chinthakad.samples.fmt.core.repository;

import com.chinthakad.samples.fmt.core.model.dto.AccountListHolder;
import io.vertx.core.Future;

public interface AccountRepository {
  Future<AccountListHolder> findAll();
}
