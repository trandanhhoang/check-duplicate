package com.example.deduplicate.library.spi.impl;

import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentEngine;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentHandler;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentPersistence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class SimpleReactiveIdempotentEngine implements
    ReactiveIdempotentEngine {

  private final IdempotentKeyExtractor keyExtractor;
  private final ReactiveIdempotentPersistence persistence;
  private final ReactiveIdempotentHandler handler;
  private final IdempotentTtlAdvisor ttlAdvisor;

  @Override
  public Mono<Object> execute(Object[] args) {
    String key = keyExtractor.resolve(args);
    return persistence.save(key, ttlAdvisor.advise())
        .switchIfEmpty(Mono.defer(() -> Mono.just(Boolean.FALSE)))
        .flatMap(successfullyPersisted -> handleResult(args, key, successfullyPersisted));
  }

  private Mono<Object> handleResult(Object[] args, String key, boolean successfullyPersisted) {
    if (successfullyPersisted) {
      log.debug("Successfully acquired idempotent key");
      return Mono.just(args);
    }
    log.debug("Idempotence detected");
    return handler.handle(args, key);
  }
}
