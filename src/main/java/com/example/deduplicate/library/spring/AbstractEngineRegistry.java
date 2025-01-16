package com.example.deduplicate.library.spring;

import com.example.deduplicate.library.annotation.Expression;
import com.example.deduplicate.library.annotation.Expression.Mode;
import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import com.example.deduplicate.library.spi.impl.FixedTtlAdvisor;
import com.example.deduplicate.library.support.expression.SpElPropertyExtractor;
import com.example.deduplicate.library.support.expression.SpElTemplateExtractor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.MethodMetadata;

public abstract class AbstractEngineRegistry {

  private final AutowireCapableBeanFactory beanFactory;

  public AbstractEngineRegistry(BeanFactory beanFactory) {
    this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
  }

  protected void discoverEngines(String packageToScan) {
    final ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(true);

    createEngineMap(packageToScan, scanner);
  }

  protected abstract void createEngineMap(
      String packageToScan, ClassPathScanningCandidateComponentProvider scanner);


  protected abstract void createEngineEntry(MethodMetadata methodMetadata);

  protected IdempotentKeyExtractor resolveIdempotentKeyExtractor(MethodMetadata methodMetadata
      , MergedAnnotation<? extends Annotation> idempotentAnnotation) {
    final MergedAnnotation<Expression> expressionAnnotation =
        idempotentAnnotation.getAnnotation("expression", Expression.class);

    final List<String> expressionStrings =
        Arrays.asList(expressionAnnotation.getStringArray("value"));

    final String prefixKey = idempotentAnnotation.getString("id");
    final Method method = resolveMethod(methodMetadata, idempotentAnnotation);

    return switch (expressionAnnotation.getEnum("mode", Mode.class)) {
      case PROPERTY -> new SpElPropertyExtractor(
          prefixKey,
          expressionStrings,
          resolvePropertySource(expressionAnnotation),
          method);
      case TEMPLATE -> new SpElTemplateExtractor(prefixKey, expressionStrings, method);
    };
  }

  private static String resolvePropertySource(MergedAnnotation<Expression> expressionAnnotation) {
    final String propertySource = expressionAnnotation.getString("propertySource");

    if (propertySource.isEmpty()) {
      throw new IllegalArgumentException("propertySource must not be empty");
    }

    return propertySource;
  }

  private static Method resolveMethod(
      MethodMetadata methodMetadata, MergedAnnotation<? extends Annotation> idempotentAnnotation) {
    final Object methodSource = idempotentAnnotation.getSource();

    if (Objects.isNull(methodSource)) {
      throw new IllegalArgumentException();
    }

    final Class<?>[] parameterTypes = resolveMethodParameterTypes(methodSource.toString());
    final Class<?> declaringClass = toClass(methodMetadata.getDeclaringClassName());

    try {
      return declaringClass.getDeclaredMethod(methodMetadata.getMethodName(), parameterTypes);
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private static Class<?>[] resolveMethodParameterTypes(String methodSignature) {
    return Stream.of(
            methodSignature
                .substring(methodSignature.indexOf('(') + 1, methodSignature.lastIndexOf(')'))
                .split(","))
        .map(AbstractEngineRegistry::toClass)
        .toArray(Class[]::new);
  }

  private static Class<?> toClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected IdempotentTtlAdvisor resolveTtlAdvisor(
      MergedAnnotation<? extends Annotation> idempotent) {

    return new FixedTtlAdvisor(
        Duration.of(idempotent.getLong("fixedDuration"),
            idempotent.getEnum("durationUnit", ChronoUnit.class)));
  }

  @SuppressWarnings("unchecked")
  protected <T> T createBean(Class<?> clazz) {
    try {
      return (T) beanFactory.getBean(clazz);
    } catch (BeansException e) {
      return (T) beanFactory.createBean(clazz);
    }
  }

}
