package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import com.github.mostroverkhov.r2.reactor.monitoring.MonitoringSupport;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.plugins.RSocketInterceptor;
import io.rsocket.util.DefaultPayload;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class RSocketMonitoringTest {
  private SimpleMeterRegistry meterRegistry;
  private Tags tags;
  private MonitoringSupport.RSocketMonitoring clientMonitoring;
  private MonitoringSupport.RSocketMonitoring serverMonitoring;

  @Before
  public void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    tags = Tags.of("test.metric", "test.value");

    clientMonitoring =
        MicrometerMonitoringSupport
            .ofClient(meterRegistry, tags)
            .rSocket();

    serverMonitoring =
        MicrometerMonitoringSupport
            .ofServer(meterRegistry, tags)
            .rSocket();
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

    Collection<Meter> meters =
        commonTags()
            .tags(
                "side", "server",
                "direction", "inbound")
            .meters();

    verifyMatchedMeters(meters);
  }

  private void interactWith(RSocketInterceptor interceptor) {
    RSocket rSocket = interceptor.apply(new TestRSocket());
    rSocket
        .fireAndForget(DefaultPayload.create("data", "metadata"))
        .block();
  }

  @NotNull
  private RequiredSearch commonTags() {
    return meterRegistry
        .get("rsocket.request.fnf")
        .tags("signal.type", "ON_COMPLETE")
        .tags(tags);
  }

  private void verifyMatchedMeters(Collection<Meter> meters) {
    Assert.assertEquals(1, meters.size());
    for (Meter meter : meters) {
      Assert.assertTrue(meter instanceof Counter);
      Counter counter = (Counter) meter;
      Assert.assertEquals(1, counter.count(), 1e-5);
    }
  }

  private static class TestRSocket extends AbstractRSocket {
    @Override
    public Mono<Void> fireAndForget(Payload payload) {
      return Mono.empty();
    }
  }
}
