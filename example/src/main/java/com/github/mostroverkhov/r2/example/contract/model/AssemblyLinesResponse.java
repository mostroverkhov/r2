package com.github.mostroverkhov.r2.example.contract.model;

public class AssemblyLinesResponse {
  private float temperature;
  private float humidity;
  private float particles;

  public AssemblyLinesResponse(float temperature, float humidity, float particles) {
    this.temperature = temperature;
    this.humidity = humidity;
    this.particles = particles;
  }

  public AssemblyLinesResponse() {
  }

  public float getTemperature() {
    return temperature;
  }

  public void setTemperature(float temperature) {
    this.temperature = temperature;
  }

  public float getHumidity() {
    return humidity;
  }

  public void setHumidity(float humidity) {
    this.humidity = humidity;
  }

  public float getParticles() {
    return particles;
  }

  public void setParticles(float particles) {
    this.particles = particles;
  }

  @Override
  public String toString() {
    return "Assembly Line{" +
        "temperature=" + temperature +
        ", humidity=" + humidity +
        ", particles=" + particles +
        '}';
  }
}
