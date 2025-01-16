package com.example.deduplicate.using;

import com.example.deduplicate.library.spi.IdempotentHandler;

public class CustomIdempotentHandler implements IdempotentHandler {

  public CustomIdempotentHandler() {
  }

  public Object handle(Object[] args, String idempotencyKey) {
    System.out.println("hoang is here");
    return null;
  }
}

