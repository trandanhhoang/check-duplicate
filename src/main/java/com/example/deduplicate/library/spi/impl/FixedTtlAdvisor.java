package com.example.deduplicate.library.spi.impl;

import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import java.time.Duration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedTtlAdvisor implements IdempotentTtlAdvisor {

  private final Duration duration;

  @Override
  public Duration advise() {

    return duration;
  }
}
