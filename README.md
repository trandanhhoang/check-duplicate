# Tự viết 1 library với annotation, aop, spring framework
- ref: https://trandanhhoang.github.io/docs/java-springboot/how-to-write-library-with-java-springboot

# Cách sử dụng
- Thêm annotation trên method cần check duplicate, thêm các thuộc tính muốn check duplicate.
```java
@Idempotent(id = "id1",
      expression = @Expression(
          value = {"name"},
          propertySource = "request")
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
```
