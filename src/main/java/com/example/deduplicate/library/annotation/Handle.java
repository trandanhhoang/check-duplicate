package com.example.deduplicate.library.annotation;

import com.example.deduplicate.library.spi.IdempotentHandler;
import com.example.deduplicate.library.spi.impl.IdempotentHandlerImpl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Handle {

    IdempotentStrategy strategy() default IdempotentStrategy.THROWING;
    Class<? extends IdempotentHandler> handler() default IdempotentHandlerImpl.class;

    enum IdempotentStrategy {

        THROWING,
        RETURNING,
    }
}
