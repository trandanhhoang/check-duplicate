package com.example.deduplicate.library.support.expression;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * A variant of the {@link SpElKeyExtractor} which focuses on a single element of the
 * {@code Object[] source} from the {@link SpElKeyExtractor#getRootObject(Object[])} input
 *
 * <p>This class defines how the targeted argument is accessed at runtime. Specifically, storing
 * the actual index of the argument, so that it can be accessed via random access during runtime.
 */
public class SpElPropertyExtractor extends SpElKeyExtractor {

  private final int propertySourceIndex;

  public SpElPropertyExtractor(
      String prefixKey,
      List<String> expressionStrings,
      String propertySource,
      Method method) {
    super(
        prefixKey,
        expressionStrings,
        new StandardEvaluationContext());
    propertySourceIndex = PropertySourceUtils.resolvePropertyIndex(propertySource, method);
  }

  /**
   * Access the root object via random access, declared at {@code propertySourceIndex}
   */
  @Override
  protected Object getRootObject(Object[] source) {
    return PropertySourceUtils.getPropertySource(source, propertySourceIndex);
  }

  /**
   * Determines the {@link ExpressionParser} may compile the expressions as regular expression
   */
  @Override
  protected ParserContext getParserContext() {
    return null;
  }
}
