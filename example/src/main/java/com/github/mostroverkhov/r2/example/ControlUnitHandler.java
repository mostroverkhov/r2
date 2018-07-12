package com.github.mostroverkhov.r2.example;

import com.github.mostroverkhov.r2.example.contract.model.ControlUnitResponse;
import com.github.mostroverkhov.r2.example.contract.services.reactor.ControlUnitService;
import reactor.core.publisher.Mono;

import java.util.Random;

public class ControlUnitHandler implements ControlUnitService {
  private static final float basePower = 42;
  private final Random random = new Random();

  @Override
  public Mono<ControlUnitResponse> power() {
    float power = basePower + 5 * random.nextFloat();
    return Mono.just(new ControlUnitResponse(power));
  }
}
