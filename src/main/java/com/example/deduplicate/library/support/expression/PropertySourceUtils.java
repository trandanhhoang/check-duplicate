package com.example.deduplicate.library.support.expression;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.stream.IntStream;

public abstract class PropertySourceUtils {

  private PropertySourceUtils() {
    throw new UnsupportedOperationException();
  }

  public static int resolvePropertyIndex(String propertySourceName, Method method) {
    final Parameter[] parameters = method.getParameters();

    return IntStream.range(0, parameters.length)
        .filter(index -> Objects.equals(propertySourceName, parameters[index].getName()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format(
                        "Unable to locate property source named %s", propertySourceName)));
  }

  public static Object getPropertySource(Object[] source, int propertySourceIndex) {
    if (Objects.isNull(source) || source.length == 0 || propertySourceIndex >= source.length) {
      return null;
    }

    return source[propertySourceIndex];
  }
}
