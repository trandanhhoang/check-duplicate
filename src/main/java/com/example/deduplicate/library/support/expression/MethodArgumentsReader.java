package com.example.deduplicate.library.support.expression;

import static java.util.Map.entry;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.NonNull;

/**
 * <strong>The goal</strong>
 *
 * <p>Is to access to method arguments via their parameter names
 *
 * <p>To achieve that, we maintain a pre-built cache, such cache maps the indexes of parameters,
 * allocated via method signature, with their parameter names. At run time, arguments are randomly
 * accessed via the mapped indexes
 */
@RequiredArgsConstructor
public class MethodArgumentsReader implements PropertyAccessor {

  private final Map<String, Integer> randomReadMapping;

  public MethodArgumentsReader(Method method) {
    this(resolveVariableMapping(method));
  }

  private static Map<String, Integer> resolveVariableMapping(Method method) {
    final Parameter[] parameters = method.getParameters();

    return IntStream.range(0, parameters.length)
        .mapToObj(index -> entry(parameters[index].getName(), index))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  @Override
  public Class<?>[] getSpecificTargetClasses() {
    return new Class[] {Object[].class};
  }

  @Override
  public boolean canRead(@NonNull EvaluationContext context, Object target, @NonNull String name) {
    return target instanceof Object[];
  }

  @NonNull
  @Override
  public TypedValue read(@NonNull EvaluationContext context, Object target, @NonNull String name) {
    if (!randomReadMapping.containsKey(name)) {
      return TypedValue.NULL;
    }

    final Object[] args = (Object[]) target;

    return new TypedValue(args[randomReadMapping.get(name)]);
  }

  @Override
  public boolean canWrite(@NonNull EvaluationContext context, Object target, @NonNull String name) {
    return false;
  }

  @Override
  public void write(
      @NonNull EvaluationContext context, Object target, @NonNull String name, Object newValue) {
    // no op
  }
}
