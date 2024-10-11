package com.example.deduplicate.library.support.expression;

import com.example.deduplicate.library.spi.IdempotentKeyExtractor;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Slf4j
public abstract class SpElKeyExtractor implements IdempotentKeyExtractor {

  public static final String COMPONENT_DELIMITER = ":";

  private final String prefixKey;

  @Getter(AccessLevel.PROTECTED)
  private final List<Expression> expressions;

  @Getter(AccessLevel.PROTECTED)
  private final EvaluationContext evaluationContext;

  @Getter(AccessLevel.PROTECTED)
  private final ExpressionParser spelExpressionParser = new SpelExpressionParser();

  protected SpElKeyExtractor(
      String prefixKey,
      List<String> expressionStrings,
      StandardEvaluationContext evaluationContext) {
    this.prefixKey = prefixKey;
    this.evaluationContext = evaluationContext;
    this.expressions = createExpressions(expressionStrings);
  }

  private List<Expression> createExpressions(List<String> expressions) {
    return expressions.stream()
        .map(expression -> spelExpressionParser.parseExpression(expression, getParserContext()))
        .map(Expression.class::cast)
        .toList();
  }

  @Override
  public String resolve(Object[] source) {
    if (Objects.isNull(source)) {
      throw new IllegalArgumentException("Source must not be null");
    }

    return String.format("%s.%s", prefixKey, collectComponents(source));
  }

  protected String collectComponents(Object[] source) {
    final String components =
        getExpressions().stream()
            .map(expression -> expression.getValue(getEvaluationContext(), getRootObject(source)))
            .map(Objects::toString)
            .collect(Collectors.joining(COMPONENT_DELIMITER));

    log.debug("Collected components: {}", components);

    return components;
  }

  protected abstract Object getRootObject(Object[] source);

  protected abstract ParserContext getParserContext();
}
