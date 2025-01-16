package com.example.deduplicate.library.spi.impl;

import com.example.deduplicate.library.spi.IdempotentHandler;

public class IdempotentHandlerImpl implements IdempotentHandler {

  @Override
  public Object handle(Object[] args, String idempotencyKey) {
    System.out.println("You can throw here an exception if you want to.");
    return null;
  }
}
