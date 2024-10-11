package com.example.deduplicate.using;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tester {

  @Autowired
  IdemController idemController;

  @PostConstruct
  public void foo() {
    idemController.idempotentTemplate("params1", new Request("huy", 23));
  }
}
