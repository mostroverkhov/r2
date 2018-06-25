package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.core.internal.ServiceMethod;
import com.github.mostroverkhov.r2.reactor.Interactions;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class InteractionsMonitoringTest {

  private SimpleMeterRegistry meterRegistry;
  private Tags tags;
  private MonitoringSupport.R2Monitoring clientMonitoring;
  private MonitoringSupport.R2Monitoring serverMonitoring;

  @Before
  public void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    tags = Tags.of("test.metric", "test.value");

    clientMonitoring =
        MicrometerMonitoringSupport
            .ofClient(meterRegistry, tags)
            .r2();

    serverMonitoring =
        MicrometerMonitoringSupport
            .ofServer(meterRegistry, tags)
            .r2();
  }

  @Test
  public void clientRequester() {

    interactWith(clientMonitoring.requester());

    Collection<Meter> meters =
        commonTags()
            .tags(
                "side", "client",
                "direction", "outbound")
            .meters();

    verifyMatchedMeters(meters);
  }

  @Test
  public void clientHandler() {

    interactWith(clientMonitoring.handler());

    Collection<Meter> meters =
        commonTags()
            .tags(
                "side", "client",
                "direction", "inbound")
            .meters();

    verifyMatchedMeters(meters);
  }

  @Test
  public void serverRequester() {

    interactWith(serverMonitoring.requester());

    Collection<Meter> meters =
        commonTags()
            .tags(
                "side", "server",
                "direction", "outbound")
            .meters();

    verifyMatchedMeters(meters);
  }

  @Test
  public void serverHandler() {

    interactWith(serverMonitoring.handler());

    Collection<Meter> metersSearch =
        commonTags()
            .tags(
                "side", "server",
                "direction", "inbound")
            .meters();

    verifyMatchedMeters(metersSearch);
  }

  private void interactWith(InteractionsInterceptor interceptor) {
    Interactions interactions = interceptor.apply(new TestInteractions());
    interactions
        .fireAndForget(new TestServiceMethod("svc", "method"))
        .block();
  }

  private RequiredSearch commonTags() {
    return meterRegistry
        .get("rsocket.r2.call")
        .tags(
            "service", "svc",
            "method", "method")
        .tags(tags)
        .tagKeys("event.type");
  }

  private void verifyMatchedMeters(Collection<Meter> meters) {
    /*meters for event.type: start, complete*/
    Assert.assertEquals(2, meters.size());
    for (Meter meter : meters) {
      Assert.assertTrue(meter instanceof Counter);
      Counter counter = (Counter) meter;
      Assert.assertEquals(1, counter.count(), 1e-5);
    }
  }

  private static class TestServiceMethod implements ServiceMethod {
    private final String service;
    private final String method;

    public TestServiceMethod(String service, String method) {
      this.service = service;
      this.method = method;
    }

    @NotNull
    @Override
    public String getService() {
      return service;
    }

    @NotNull
    @Override
    public String getMethod() {
      return method;
    }
  }

  private static class TestInteractions implements Interactions {

    @Override
    public Mono<Void> fireAndForget(ServiceMethod call) {
      return Mono.empty();
    }

    @Override
    public Mono<?> requestResponse(ServiceMethod call) {
      return Mono.empty();
    }

    @Override
    public Flux<?> requestStream(ServiceMethod call) {
      return Flux.empty();
    }

    @Override
    public Flux<?> requestChannel(ServiceMethod call) {
      return Flux.empty();
    }

    @Override
    public Mono<Void> close() {
      return Mono.empty();
    }

    @Override
    public Mono<Void> onClose() {
      return Mono.empty();
    }
  }

}
