package com.example.deduplicate.library.spi.impl;

import com.example.deduplicate.library.spi.IdempotentEngine;
import com.example.deduplicate.library.spi.IdempotentHandler;
import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentPersistence;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SimpleIdempotentEngine implements IdempotentEngine {

  private final IdempotentKeyExtractor idempotentKeyExtractor;
  private final IdempotentPersistence idempotentPersistence;
  private final IdempotentHandler idempotentHandler;
  private final IdempotentTtlAdvisor idempotentTtlAdvisor;

  @Override
  public Object execute(Object[] args) {
    String key = idempotentKeyExtractor.resolve(args);
    boolean successfullyPersisted = idempotentPersistence.save(key, idempotentTtlAdvisor.advise());
    if (successfullyPersisted) {
      log.info("Successfully acquired idempotent key");
      return null;
    }
    log.info("Idempotence detected");
    return idempotentHandler.handle(args, key);
  }
}
