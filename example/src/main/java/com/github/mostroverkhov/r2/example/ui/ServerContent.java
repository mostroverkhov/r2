package com.github.mostroverkhov.r2.example.ui;

import de.vandermeer.asciitable.AsciiTable;

import static com.github.mostroverkhov.r2.example.ui.AssemblyLineRenderer.State;

public class ServerContent implements Content {
  private final State state;

  public ServerContent(State state) {
    this.state = state;
  }

  @Override
  public String render() {
    StringBuilder sb = new StringBuilder();
    sb.append("SERVER: ")
        .append(changeCaption(state.getChange()))
        .append("\n");

    AsciiTable at = new AsciiTable();
    at.addRule();
    at.addRow("Available Power", "Assembly lines", "Authentication");
    at.addRule();
    at.addRow(state.getPower(), state.getAssemblyLines(), state.getAuth());
    at.addRule();
    return sb.append(at.render(65)).toString();
  }

  private String changeCaption(State.Change change) {
    switch (change) {
      case LINES:
        return "Assembly lines received";
      case POWER:
        return "Control unit power received";
      case INIT:
      default:
        return "";
    }
  }
}
