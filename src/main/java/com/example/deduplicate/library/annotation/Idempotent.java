package com.example.deduplicate.library.annotation;

import com.example.deduplicate.library.spi.IdempotentPersistence;
import com.example.deduplicate.library.spi.impl.IdempotentPersistenceImpl;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target(java.lang.annotation.ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Idempotent {

  String id();

  Expression expression();

  Class<? extends IdempotentPersistence> persistent() default IdempotentPersistenceImpl.class;

  Handle handle() default @Handle;

  long fixedDuration() default 1;

  ChronoUnit durationUnit() default ChronoUnit.MINUTES;
}
