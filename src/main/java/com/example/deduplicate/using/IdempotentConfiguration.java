package com.example.deduplicate.using;

import com.example.deduplicate.library.spring.IdempotentConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(IdempotentConstructor.class)
public class IdempotentConfiguration {

}
