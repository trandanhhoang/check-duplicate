package com.example.deduplicate.using;

import com.example.deduplicate.library.annotation.Expression;
import com.example.deduplicate.library.annotation.Expression.Mode;
import com.example.deduplicate.library.annotation.Handle;
import com.example.deduplicate.library.annotation.Idempotent;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(IdempotentConfiguration.class)
public class IdemController {

  @Idempotent(id = "id1",
      expression = @Expression(
          value = {"name"},
          propertySource = "request"),
      handle = @Handle(
          handler = CustomIdempotentHandler.class
      )
  )
  public void idempotent(String params1, Request request) {
  }

  @Idempotent(id = "id2",
      expression = @Expression(
          value = {"#{request.name}",
              "#{params1}"},
          propertySource = "request",
      mode = Mode.TEMPLATE),
      fixedDuration = 10,
      handle = @Handle(
          handler = CustomIdempotentHandler.class
      )
  )
  public void idempotentTemplate(String params1, Request request) {
  }
}
