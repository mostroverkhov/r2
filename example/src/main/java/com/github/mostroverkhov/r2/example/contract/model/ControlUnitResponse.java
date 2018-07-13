package com.github.mostroverkhov.r2.example.contract.model;

public class ControlUnitResponse {
  float availablePower;

  public ControlUnitResponse(float availablePower) {
    this.availablePower = availablePower;
  }

  public ControlUnitResponse() {
  }

  public float getAvailablePower() {
    return availablePower;
  }

  public void setAvailablePower(float availablePower) {
    this.availablePower = availablePower;
  }

  @Override
  public String toString() {
    return "Response{" +
        "availablePower=" + availablePower +
        '}';
  }

}
