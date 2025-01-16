package com.example.deduplicate.library.spring;

import com.example.deduplicate.library.aop.IdempotentAOP;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;

public class IdempotentConstructor {

  @Bean
  public IdempotentEngineRegistry idempotentEngineRegistry(BeanFactory beanFactory) {
    return new IdempotentEngineRegistry(beanFactory);
  }

  @Bean
  public IdempotentAOP createIdempotentAOP(IdempotentEngineRegistry idempotentEngineRegistry) {
    return new IdempotentAOP(idempotentEngineRegistry);
  }
}
