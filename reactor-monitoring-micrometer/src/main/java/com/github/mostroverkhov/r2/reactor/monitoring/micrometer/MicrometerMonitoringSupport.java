package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.Collections;
import java.util.Objects;

public final class MicrometerMonitoringSupport implements MonitoringSupport {
  private final MeterRegistry meterRegistry;
  private final Iterable<Tag> tags;

  public static Builder builder() {
    return new Builder();
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

  public static class Builder {
    private static final Tag serverSide = side("server");
    private static final Tag clientSide = side("client");
    private static final Iterable<Tag> emptyTags = Collections.emptyList();

    private Iterable<Tag> tags = emptyTags;
    private MeterRegistry meterRegistry;

    private Builder() {
    }

    public Builder tags(Iterable<Tag> tags) {
      this.tags = Objects.requireNonNull(tags, "Tags");
      return this;
    }

    public Builder meterRegistry(MeterRegistry meterRegistry) {
      this.meterRegistry = Objects.requireNonNull(
          meterRegistry,
          "MeterRegistry");
      return this;
    }

    public MonitoringSupport forClient() {
      return monitorWithTag(clientSide);
    }

    public MonitoringSupport forServer() {
      return monitorWithTag(serverSide);
    }

    private MonitoringSupport monitorWithTag(Tag tag) {
      checkState();
      return new MicrometerMonitoringSupport(
          meterRegistry,
          Tags.of(tags).and(tag));
    }

    private void checkState() {
      if (meterRegistry == null) {
        throw new IllegalStateException("MeterRegistry must be set");
      }
    }

    private static Tag side(String side) {
      return Tag.of("side", side);
    }
  }
}
