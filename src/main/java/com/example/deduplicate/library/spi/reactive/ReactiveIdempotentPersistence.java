package com.example.deduplicate.library.spi.reactive;

import java.time.Duration;
import reactor.core.publisher.Mono;

public interface ReactiveIdempotentPersistence {

  Mono<Boolean> save(String key, Duration ttl);
}
