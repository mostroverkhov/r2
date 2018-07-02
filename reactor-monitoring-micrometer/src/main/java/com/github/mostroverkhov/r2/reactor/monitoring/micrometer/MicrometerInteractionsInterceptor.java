package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.Interactions;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

class MicrometerInteractionsInterceptor implements InteractionsInterceptor {
  private final MeterRegistry meterRegistry;
  private final Tag[] tags;

  public MicrometerInteractionsInterceptor(MeterRegistry meterRegistry,
                                           Tag[] tags) {
    this.meterRegistry = meterRegistry;
    this.tags = tags;
  }

  @Override
  public Interactions apply(Interactions interactions) {
    return new MicrometerInteractions(interactions, meterRegistry, tags);
  }
}
