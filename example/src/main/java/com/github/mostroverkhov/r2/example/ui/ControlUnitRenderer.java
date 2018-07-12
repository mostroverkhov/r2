package com.github.mostroverkhov.r2.example.ui;

import com.github.mostroverkhov.r2.example.contract.model.AssemblyLinesResponse;

public class ControlUnitRenderer {

  public void assemblyLineStateChanged(AssemblyLinesResponse resp) {
    ClientContent content = new ClientContent(resp);
    System.out.println(content.render());
  }

  public void assemblyLineError(Throwable throwable) {
    System.out.println("Stream error: " + throwable);
  }
}
