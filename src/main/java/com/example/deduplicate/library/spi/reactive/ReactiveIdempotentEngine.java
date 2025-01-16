package com.example.deduplicate.library.spi.reactive;

import reactor.core.publisher.Mono;

public interface ReactiveIdempotentEngine {

  Mono<Object> execute(Object[] args);
}
