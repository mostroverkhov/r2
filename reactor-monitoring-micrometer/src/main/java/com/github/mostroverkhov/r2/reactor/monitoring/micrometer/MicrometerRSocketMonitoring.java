package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.rsocket.micrometer.MicrometerDuplexConnectionInterceptor;
import io.rsocket.micrometer.MicrometerRSocketInterceptor;
import io.rsocket.plugins.DuplexConnectionInterceptor;
import io.rsocket.plugins.RSocketInterceptor;

class MicrometerRSocketMonitoring extends MicrometerTags
    implements MonitoringSupport.RSocketMonitoring {

  public MicrometerRSocketMonitoring(MeterRegistry meterRegistry,
                                     Iterable<Tag> tags) {
    super(meterRegistry, tags);
  }

  @Override
  public DuplexConnectionInterceptor connection() {
    return new MicrometerDuplexConnectionInterceptor(
        meterRegistry,
        toTagsArray());
  }

  @Override
  public RSocketInterceptor requester() {
    return new MicrometerRSocketInterceptor(
        meterRegistry,
        withOutboundTags());
  }

  @Override
  public RSocketInterceptor handler() {
    return new MicrometerRSocketInterceptor(
        meterRegistry,
        withInboundTags());
  }
}
