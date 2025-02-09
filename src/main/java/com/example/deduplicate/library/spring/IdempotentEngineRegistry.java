package com.example.deduplicate.library.spring;

import com.example.deduplicate.library.annotation.Handle;
import com.example.deduplicate.library.annotation.Idempotent;
import com.example.deduplicate.library.spi.IdempotentEngine;
import com.example.deduplicate.library.spi.IdempotentHandler;
import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import com.example.deduplicate.library.spi.IdempotentPersistence;
import com.example.deduplicate.library.spi.IdempotentTtlAdvisor;
import com.example.deduplicate.library.spi.impl.SimpleIdempotentEngine;
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

public class IdempotentEngineRegistry extends AbstractEngineRegistry {

  private final Map<String, IdempotentEngine> idempotentEngines = new HashMap<>();

  public IdempotentEngineRegistry(BeanFactory beanFactory) {
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
        Set<MethodMetadata> methodMetadatas = annotationMetadata.getAnnotatedMethods(
            Idempotent.class.getCanonicalName());
        for (MethodMetadata methodMetadata : methodMetadatas) {
          createEngineEntry(methodMetadata);
        }
      }
    }
  }

  @Override
  protected void createEngineEntry(MethodMetadata methodMetadata) {
    final MergedAnnotation<? extends Annotation> annotationMetadata =
        methodMetadata.getAnnotations().get(Idempotent.class);
    final IdempotentKeyExtractor keyExtractor = resolveIdempotentKeyExtractor(methodMetadata,
        annotationMetadata);
    final IdempotentPersistence idempotencePersistence = createBean(
        annotationMetadata.getClass("persistent"));
    final IdempotentHandler idempotenceHandler = createBean(
        annotationMetadata.getAnnotation("handle", Handle.class).getClass("handler"));
    final IdempotentTtlAdvisor idempotentTtlAdvisor =
        resolveTtlAdvisor(annotationMetadata);

    idempotentEngines.put(annotationMetadata.getString("id"),

        new SimpleIdempotentEngine(keyExtractor
            , idempotencePersistence, idempotenceHandler, idempotentTtlAdvisor));
  }

  public IdempotentEngine get(String name) {
    return idempotentEngines.get(name);
  }

}
