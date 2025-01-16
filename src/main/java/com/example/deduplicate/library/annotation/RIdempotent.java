package com.example.deduplicate.library.annotation;

import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentPersistence;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target(java.lang.annotation.ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface RIdempotent {

  String id();

  Expression expression();

  Class<? extends ReactiveIdempotentPersistence> persistent();

  RHandle handle() default @RHandle;

  long fixedDuration() default 24;

  ChronoUnit durationUnit() default ChronoUnit.HOURS;
}
