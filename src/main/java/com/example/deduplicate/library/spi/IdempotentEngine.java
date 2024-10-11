package com.example.deduplicate.library.spi;

public interface IdempotentEngine {
    Object execute(Object[] args);
}
