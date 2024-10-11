package com.example.deduplicate.library.spi;

public interface IdempotentKeyExtractor {
    String resolve(Object[] args);
}
