package com.github.mostroverkhov.r2.reactor.monitoring;

import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import io.rsocket.plugins.DuplexConnectionInterceptor;
import io.rsocket.plugins.RSocketInterceptor;

public interface MonitoringSupport {

  R2Monitoring r2();

  RSocketMonitoring rSocket();

  interface R2Monitoring {

    InteractionsInterceptor requester();

    InteractionsInterceptor handler();
  }

  interface RSocketMonitoring {

    DuplexConnectionInterceptor connection();

    RSocketInterceptor requester();

    RSocketInterceptor handler();
  }
}
