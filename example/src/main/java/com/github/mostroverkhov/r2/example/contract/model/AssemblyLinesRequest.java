package com.github.mostroverkhov.r2.example.contract.model;

public class AssemblyLinesRequest {
  private int activeAssemblies;

  public AssemblyLinesRequest(int activeAssemblies) {
    this.activeAssemblies = activeAssemblies;
  }

  public AssemblyLinesRequest() {
  }

  public int getActiveAssemblies() {
    return activeAssemblies;
  }

  public void setActiveAssemblies(int activeAssemblies) {
    this.activeAssemblies = activeAssemblies;
  }

  @Override
  public String toString() {
    return "Request{" +
        "activeAssemblies=" + activeAssemblies +
        '}';
  }

}
