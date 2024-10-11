package com.example.deduplicate.library.support.expression;

import java.lang.reflect.Method;
import java.util.List;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * <p><strong>Usages:</strong>
 *
 * <blockquote>
 *
 * <pre>{@code
 * @Idempotent(
 *   id = "handle-new-post",
 *   expression = @Expression(value = { "#{post.content}", "#{createdBy}" }, mode = Mode.TEMPLATE))
 * public Post handleNewPost(Post post, String createdBy) {
 *   // ...
 * }
 * }</pre>
 *
 * </blockquote>
 * <p>
 * With the above setup, in combination with an instance of this class, usages may have access to
 * both the {@code post.*} and {@code createdBy} argument declared on the {@code handleNewPost}
 * method
 */
public class SpElTemplateExtractor extends SpElKeyExtractor {

  private static final ParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext();

  public SpElTemplateExtractor(String prefixKey, List<String> expressionStrings, Method method) {
    super(prefixKey, expressionStrings, resolveEvaluationContext(method));
  }

  private static StandardEvaluationContext resolveEvaluationContext(Method method) {
    final StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();

    standardEvaluationContext.addPropertyAccessor(new MethodArgumentsReader(method));

    return standardEvaluationContext;
  }

  /**
   * With this powerful extractor being able to locate any arguments, we can just use the whole
   * input as the root object
   */
  @Override
  protected Object getRootObject(Object[] source) {
    return source;
  }

  /**
   * We use the default SpEl templating syntax here
   *
   * @see TemplateParserContext#TemplateParserContext()
   */
  @Override
  protected ParserContext getParserContext() {
    return TEMPLATE_PARSER_CONTEXT;
  }
}
