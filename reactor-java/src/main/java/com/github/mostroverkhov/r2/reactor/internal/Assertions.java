package com.github.mostroverkhov.r2.reactor.internal;

import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;

public class Assertions {
  private final String prefix;

  private Assertions(String prefix) {
    this.prefix = prefix;
  }

  public static Assertions forClient() {
    return new Assertions("Client.");
  }

  public static Assertions forServer() {
    return new Assertions("Server.");
  }

  public void assertMonitoring(MonitoringSupport monitoringSupport) {
    if (monitoringSupport != null) {
      MonitoringSupport.R2Monitoring r2 = monitoringSupport.r2();
      assertArg(r2, "monitoring.r2");
      assertArg(r2.requester(), "monitoring.r2.requester");
      assertArg(r2.handler(), "monitoring.r2.handler");

      MonitoringSupport.RSocketMonitoring rSocket = monitoringSupport.rSocket();
      assertArg(rSocket, "monitoring.rSocket");
      assertArg(rSocket.connection(), "monitoring.rSocket.connection");
      assertArg(rSocket.requester(), "monitoring.rSocket.requester");
      assertArg(rSocket.handler(), "monitoring.rSocket.handler");
    }
  }

  public void assertArg(Object arg, String name) {
    if (arg == null) {
      throw new IllegalArgumentException(prefixedMsg(name) + " was not set");
    }
  }

  private String prefixedMsg(String msg) {
    return prefix + msg;
  }
}
