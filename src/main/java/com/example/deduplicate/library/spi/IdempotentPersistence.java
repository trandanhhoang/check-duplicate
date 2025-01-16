package com.example.deduplicate.library.spi;

import java.time.Duration;

public interface IdempotentPersistence {

  boolean save(String key, Duration ttl);
}
