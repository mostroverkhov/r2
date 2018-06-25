package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.Objects;

public final class MicrometerMonitoringSupport implements MonitoringSupport {

  private static final Tag serverSide = side("server");
  private static final Tag clientSide = side("client");
  private final MeterRegistry meterRegistry;
  private final Iterable<Tag> tags;

  public static MonitoringSupport ofServer(MeterRegistry meterRegistry,
                                           Iterable<Tag> tags) {
    assertArgs(meterRegistry, tags);
    return new MicrometerMonitoringSupport(meterRegistry, Tags.of(tags).and(serverSide));
  }

  public static MonitoringSupport ofClient(MeterRegistry meterRegistry,
                                           Iterable<Tag> tags) {
    assertArgs(meterRegistry, tags);
    return new MicrometerMonitoringSupport(meterRegistry, Tags.of(tags).and(clientSide));
  }

  private MicrometerMonitoringSupport(MeterRegistry meterRegistry,
                                      Iterable<Tag> tags) {
    this.meterRegistry = meterRegistry;
    this.tags = tags;
  }

  @Override
  public R2Monitoring r2() {
    return new MicrometerMonitoring(
        meterRegistry,
        tags
    );
  }

  @Override
  public RSocketMonitoring rSocket() {
    return new MicrometerRSocketMonitoring(
        meterRegistry,
        tags
    );
  }

  private static Tag side(String side) {
    return Tag.of("side", side);
  }

  private static void assertArgs(MeterRegistry meterRegistry,
                                 Iterable<Tag> tags) {
    Objects.requireNonNull(meterRegistry, "MeterRegistry");
    Objects.requireNonNull(tags, "Tags");
  }
}
