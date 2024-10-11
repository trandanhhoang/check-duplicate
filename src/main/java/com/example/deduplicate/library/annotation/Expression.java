package com.example.deduplicate.library.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p><strong>Usages:</strong>
 * <blockquote>
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
 *
 * <blockquote>
 * * <pre>{@code
 *  * @Idempotent(
 *  *   id = "property",
 *  *   expression = @Expression(value = {
 *               "content",
 *               "#xid()"
 *           },, propertySource = "post"))
 *  * public Post handleNewPost(Post post, String createdBy) {
 *  *   // ...
 *  * }
 *  * }</pre>
 * * * </blockquote>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {

  String[] value() default "";

  Mode mode() default Mode.PROPERTY;

  /**
   * When in {@link Mode#PROPERTY} mode, specifies the property source target
   */
  String propertySource() default "";

  enum Mode {

    /**
     * Takes every method arguments
     */
    TEMPLATE,

    /**
     * only one argument is used
     */
    PROPERTY
  }
}
