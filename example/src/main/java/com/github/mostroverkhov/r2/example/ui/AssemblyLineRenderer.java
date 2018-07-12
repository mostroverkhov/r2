package com.github.mostroverkhov.r2.example.ui;

import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesRequest;
import com.github.mostroverkhov.r2.example.contract.model.ControlUnitResponse;

public class AssemblyLineRenderer {

  private final State state = new State();
  private final ServerContent serverContent = new ServerContent(state);

  public void assemblyLinesChanged(AssemblyLinesRequest request) {
    state.setAssemblyLines(request.getActiveAssemblies());
    render();
  }

  public void powerChanged(ControlUnitResponse resp) {
    state.setPower(resp.getAvailablePower());
    render();
  }

  public void powerError(Throwable e) {
    System.out.println("Stream error: " + e);
  }

  public void authChanged(byte[] auth) {
    state.setAuth(new String(auth));
    render();
  }

  private void render() {
    System.out.println(serverContent.render());
  }

  public static class State {
    private int assemblyLines;
    private float power;
    private String auth = "";
    private State.Change change = State.Change.INIT;

    public int getAssemblyLines() {
      return assemblyLines;
    }

    public State setAssemblyLines(int assemblyLines) {
      this.assemblyLines = assemblyLines;
      change = State.Change.LINES;
      return this;
    }

    public float getPower() {
      return power;
    }

    public State setPower(float power) {
      this.power = power;
      change = State.Change.POWER;
      return this;
    }

    public String getAuth() {
      return auth;
    }

    public State setAuth(String auth) {
      this.auth = auth;
      return this;
    }

    public State.Change getChange() {
      return change;
    }

    public enum Change {
      INIT, LINES, POWER
    }
  }
}
