package com.github.mostroverkhov.r2.example;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

class AssemblyLineModel {

  private static final float[] temperatures = {18, 21, 24, 29};
  private static final float[] humidities = {0.3f, 0.5f, 0.6f, 0.7f};
  private static final float[] particles = {1000, 1500, 2000, 4000};

  private final Random random = new Random();

  @NotNull
  public Contract.AssemblyLines.Response monitoringResponse(Integer assemblies) {
    int count = Math.max(0, Math.min(3, assemblies));
    float temp = nextTemp(count);
    float humidity = nextHumidity(count);
    float particles = nextParticles(count);

    return new Contract.AssemblyLines.Response(temp, humidity, particles);
  }

  private float nextTemp(int index) {
    return next(temperatures, index, 2);
  }

  private float nextHumidity(int index) {
    return next(humidities, index, 0.2f);
  }

  private float nextParticles(int index) {
    return next(particles, index, 1000);
  }

  private float next(float[] arr, int index, float coef) {
    return arr[index] + coef * random.nextFloat();
  }
}
