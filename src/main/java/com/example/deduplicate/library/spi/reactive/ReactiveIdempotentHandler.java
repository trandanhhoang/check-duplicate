package com.example.deduplicate.library.spi.reactive;

import reactor.core.publisher.Mono;

public interface ReactiveIdempotentHandler {

  Mono<Object> handle(Object[] args, String idempotencyKey);
}
