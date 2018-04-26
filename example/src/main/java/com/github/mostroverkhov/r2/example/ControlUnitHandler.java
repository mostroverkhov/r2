package com.github.mostroverkhov.r2.example;

import reactor.core.publisher.Mono;

import java.util.Random;

import static com.github.mostroverkhov.r2.example.Contract.ControlUnit;
import static com.github.mostroverkhov.r2.example.Contract.ControlUnit.Response;

public class ControlUnitHandler implements ControlUnit.Svc {
  private static final float basePower = 42;
  private final Random random = new Random();

  @Override
  public Mono<Response> power() {
    float power = basePower + 5 * random.nextFloat();
    return Mono.just(new Response(power));
  }
}
