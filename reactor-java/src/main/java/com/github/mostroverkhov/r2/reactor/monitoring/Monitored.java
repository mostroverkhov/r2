package com.github.mostroverkhov.r2.reactor.monitoring;

public interface Monitored<T> {

  T monitor(MonitoringSupport monitoringSupport);
}
