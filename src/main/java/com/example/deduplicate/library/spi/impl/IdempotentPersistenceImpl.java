package com.example.deduplicate.library.spi.impl;

import com.example.deduplicate.library.spi.IdempotentPersistence;
import com.example.deduplicate.library.support.redis.AbstractRedisPersistence;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

public class IdempotentPersistenceImpl extends AbstractRedisPersistence implements
    IdempotentPersistence {

  private final RedisTemplate<String, String> redisTemplate;

  public IdempotentPersistenceImpl(
      @Value("${idempotence.persistence.schema:}") String schema,
      RedisTemplate<String, String> redisTemplate,
      Environment environment) {

    super(determineSchema(schema, environment));
    this.redisTemplate = redisTemplate;
  }

  // Helper method to determine the schema
  private static String determineSchema(String schema, Environment environment) {
    return (schema == null || schema.isEmpty())
        ? environment.getProperty("spring.application.name", "idempotence")
        : schema;
  }

  @Override
  public boolean save(String key, Duration expire) {
    final String persistenceKey = resolveKey(key);
    final Boolean result = redisTemplate.opsForValue().setIfAbsent(persistenceKey, key, expire);
    return Optional.ofNullable(result).orElse(false);
  }
}
