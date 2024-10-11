package com.example.deduplicate.library.spi;

public interface IdempotentHandler {
    Object handle(Object[] args);
}
