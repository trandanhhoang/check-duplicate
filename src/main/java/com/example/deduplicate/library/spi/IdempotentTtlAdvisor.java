package com.example.deduplicate.library.spi;

import java.time.Duration;

public interface IdempotentTtlAdvisor {

  Duration advise();
}
