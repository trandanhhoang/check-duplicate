package com.example.deduplicate.library.aop;

import com.example.deduplicate.library.annotation.Idempotent;
import com.example.deduplicate.library.spring.IdempotentEngineRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class IdempotentAOP {

  private final IdempotentEngineRegistry idempotentEngineRegistry;

  public IdempotentAOP(IdempotentEngineRegistry idempotentEngineRegistry) {
    this.idempotentEngineRegistry = idempotentEngineRegistry;
  }

  @Before("@annotation(com.example.deduplicate.library.annotation.Idempotent)")
  public Object before(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Idempotent annotation = signature.getMethod().getDeclaredAnnotation(Idempotent.class);
    String key = annotation.id();
    return idempotentEngineRegistry.get(key).execute(joinPoint.getArgs());
  }
}
