package com.example.deduplicate.library.support.redis;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRedisPersistence {

  private final String schema;

  protected String resolveKey(String key) {
    return String.format("%s.%s", schema, key);
  }
}
