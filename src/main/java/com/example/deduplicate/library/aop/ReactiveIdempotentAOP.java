package com.example.deduplicate.library.aop;

import com.example.deduplicate.library.annotation.RIdempotent;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentEngine;
import com.example.deduplicate.library.spring.ReactiveIdempotentEngineRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import reactor.core.publisher.Mono;

@Aspect
public class ReactiveIdempotentAOP {

  private final ReactiveIdempotentEngineRegistry registry;

  public ReactiveIdempotentAOP(ReactiveIdempotentEngineRegistry registry) {
    this.registry = registry;
  }

  @Around("@annotation(com.example.deduplicate.library.annotation.RIdempotent)")
  public Mono<Object> aroundAdvice(ProceedingJoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    RIdempotent annotation = signature.getMethod().getDeclaredAnnotation(RIdempotent.class);
    String key = annotation.id();
    ReactiveIdempotentEngine engine = registry.get(key);

    return engine.execute(joinPoint.getArgs()).then(proceed(joinPoint));
  }

  private Mono<Object> proceed(ProceedingJoinPoint joinPoint) {
    return Mono.defer(() -> {
      try {
        return (Mono<Object>) joinPoint.proceed(joinPoint.getArgs());
      } catch (Throwable e) {
        return Mono.error(e);
      }
    });
  }
}
