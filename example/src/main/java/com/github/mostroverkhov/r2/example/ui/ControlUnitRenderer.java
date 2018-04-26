package com.github.mostroverkhov.r2.example.ui;

import com.github.mostroverkhov.r2.example.Contract;

public class ControlUnitRenderer {

  public void assemblyLineStateChanged(Contract.AssemblyLines.Response resp) {
    ClientContent content = new ClientContent(resp);
    System.out.println(content.render());
  }

  public void assemblyLineError(Throwable throwable) {
    System.out.println("Stream error: " + throwable);
  }
}
