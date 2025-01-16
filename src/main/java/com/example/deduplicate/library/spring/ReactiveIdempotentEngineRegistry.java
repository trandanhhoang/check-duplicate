package com.example.deduplicate.library.spring;

import com.example.deduplicate.library.annotation.Handle;
import com.example.deduplicate.library.annotation.RIdempotent;
import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import com.example.deduplicate.library.spi.impl.SimpleReactiveIdempotentEngine;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentEngine;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentHandler;
import com.example.deduplicate.library.spi.reactive.ReactiveIdempotentPersistence;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;

public class ReactiveIdempotentEngineRegistry extends AbstractEngineRegistry {

  private final Map<String, ReactiveIdempotentEngine> reactiveIdempotentEngines = new HashMap<>();

  public ReactiveIdempotentEngineRegistry(BeanFactory beanFactory) {
    super(beanFactory);
    discoverEngines("com.example.deduplicate.using");
  }

  @Override
  protected void createEngineMap(
      String packageToScan, ClassPathScanningCandidateComponentProvider scanner) {
    Set<BeanDefinition> listBean = scanner.findCandidateComponents(packageToScan);

    for (BeanDefinition beanDefinition : listBean) {
      if (beanDefinition instanceof AnnotatedBeanDefinition) {
        AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
        Set<MethodMetadata> metadataSet = annotationMetadata.getAnnotatedMethods(
            RIdempotent.class.getCanonicalName());
        for (MethodMetadata methodMetadata : metadataSet) {
          createEngineEntry(methodMetadata);
        }
      }
    }
  }

  @Override
  protected void createEngineEntry(MethodMetadata methodMetadata) {
    final MergedAnnotation<? extends Annotation> annotationMetadata =
        methodMetadata.getAnnotations().get(RIdempotent.class);
    final IdempotentKeyExtractor keyExtractor = resolveIdempotentKeyExtractor(methodMetadata,
        annotationMetadata);
    final ReactiveIdempotentPersistence persistent = createBean(
        annotationMetadata.getClass("persistent"));
    final ReactiveIdempotentHandler handler = createBean(
        annotationMetadata.getAnnotation("handle", Handle.class).getClass("handler"));
    final IdempotentTtlAdvisor idempotentTtlAdvisor =
        resolveTtlAdvisor(annotationMetadata);

    reactiveIdempotentEngines.put(annotationMetadata.getString("id"),
        new SimpleReactiveIdempotentEngine(keyExtractor
            , persistent, handler, idempotentTtlAdvisor));
  }

  public ReactiveIdempotentEngine get(String name) {
    return reactiveIdempotentEngines.get(name);
  }

}
