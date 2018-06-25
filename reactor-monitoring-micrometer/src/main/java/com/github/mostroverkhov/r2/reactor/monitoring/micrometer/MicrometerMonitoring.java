package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

class MicrometerMonitoring extends MicrometerTags implements
    MonitoringSupport.R2Monitoring {

  public MicrometerMonitoring(MeterRegistry meterRegistry,
                              Iterable<Tag> tags) {
    super(meterRegistry, tags);
  }

  @Override
  public InteractionsInterceptor requester() {
    return new MicrometerInteractionsInterceptor(
        meterRegistry,
        withOutboundTags());
  }

  @Override
  public InteractionsInterceptor handler() {
    return new MicrometerInteractionsInterceptor(
        meterRegistry,
        withInboundTags()
    );
  }
}
