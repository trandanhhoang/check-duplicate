package com.example.deduplicate.library.spring;

import static com.example.deduplicate.library.annotation.Handle.IdempotentStrategy.THROWING;

import com.example.deduplicate.library.annotation.Expression;
import com.example.deduplicate.library.annotation.Expression.Mode;
import com.example.deduplicate.library.annotation.Handle;
import com.example.deduplicate.library.annotation.Idempotent;
import com.example.deduplicate.library.spi.IdempotentEngine;
import com.example.deduplicate.library.spi.IdempotentHandler;
import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentPersistence;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import com.example.deduplicate.library.spi.impl.FixedTtlAdvisor;
import com.example.deduplicate.library.spi.impl.IdempotentHandlerImpl;
import com.example.deduplicate.library.spi.impl.SimpleIdempotentEngine;
import com.example.deduplicate.library.support.expression.SpElPropertyExtractor;
import com.example.deduplicate.library.support.expression.SpElTemplateExtractor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

public class IdempotentEngineRegistry implements BeanPostProcessor {

  private final Map<String, IdempotentEngine> idempotentEngines = new HashMap<>();
  private final AutowireCapableBeanFactory beanFactory;

  public IdempotentEngineRegistry(BeanFactory beanFactory) {
    this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    discoverEngines("com.example.deduplicate.using");
  }

  private void discoverEngines(String packageToScan) {
    final ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(true);

    createEngineMap(packageToScan, scanner);
  }

  private void createEngineMap(
      String packageToScan, ClassPathScanningCandidateComponentProvider scanner) {
    Set<BeanDefinition> listBean = scanner.findCandidateComponents(packageToScan);

    for (BeanDefinition beanDefinition : listBean) {
      if (beanDefinition instanceof AnnotatedBeanDefinition) {
        AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
        Set<MethodMetadata> methodMetadatas = annotationMetadata.getAnnotatedMethods(
            Idempotent.class.getCanonicalName());
        for (MethodMetadata methodMetadata : methodMetadatas) {
          createEngineEntry(methodMetadata);
        }
      }
    }
  }

  private void createEngineEntry(MethodMetadata methodMetadata) {
    final MergedAnnotation<? extends Annotation> annotationMetadata =
        methodMetadata.getAnnotations().get(Idempotent.class);
    final IdempotentKeyExtractor keyExtractor = resolveIdempotentKeyExtractor(methodMetadata,
        annotationMetadata);
    final IdempotentPersistence idempotencePersistence = resolveIdempotentPersistant(
        annotationMetadata);
    final IdempotentHandler idempotenceHandler = resolveIdempotentHandler(
        annotationMetadata.getAnnotation("handle", Handle.class));
    final IdempotentTtlAdvisor idempotentTtlAdvisor =
        resolveTtlAdvisor(annotationMetadata);

    idempotentEngines.put(annotationMetadata.getString("id"),

        new SimpleIdempotentEngine(keyExtractor
            , idempotencePersistence, idempotenceHandler, idempotentTtlAdvisor));
  }

  private IdempotentKeyExtractor resolveIdempotentKeyExtractor(MethodMetadata methodMetadata
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
        .map(IdempotentEngineRegistry::toClass)
        .toArray(Class[]::new);
  }

  private static Class<?> toClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private IdempotentPersistence resolveIdempotentPersistant(
      MergedAnnotation<? extends Annotation> idempotent) {
    return createBean(idempotent.getClass("persistent"));
  }

  private IdempotentHandler resolveIdempotentHandler(
      MergedAnnotation<? extends Annotation> handleAnnotation) {

    Handle.IdempotentStrategy strategy = handleAnnotation.getEnum("strategy",
        Handle.IdempotentStrategy.class);
//    if (THROWING.equals(strategy)) {
//      return new IdempotentHandlerImpl();
//    }

    return createBean(handleAnnotation.getClass("handler"));
  }

  protected IdempotentTtlAdvisor resolveTtlAdvisor(
      MergedAnnotation<? extends Annotation> idempotent) {

    return new FixedTtlAdvisor(
        Duration.of(idempotent.getLong("fixedDuration"),
            idempotent.getEnum("durationUnit", ChronoUnit.class)));
  }

  @SuppressWarnings("unchecked")
  private <T> T createBean(Class<?> clazz) {
    try {
      return (T) beanFactory.getBean(clazz);
    } catch (BeansException e) {
      return (T) beanFactory.createBean(clazz);
    }
  }

  public IdempotentEngine get(String name) {
    return idempotentEngines.get(name);
  }

}
