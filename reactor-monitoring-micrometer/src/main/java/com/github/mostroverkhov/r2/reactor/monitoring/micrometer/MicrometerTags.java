package com.github.mostroverkhov.r2.reactor.monitoring.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class MicrometerTags {
  final Tags tags;
  final MeterRegistry meterRegistry;

  public MicrometerTags(MeterRegistry meterRegistry,
                        Iterable<Tag> tags) {
    Objects.requireNonNull(meterRegistry, "MeterRegistry");
    Objects.requireNonNull(tags, "Tags");
    this.meterRegistry = meterRegistry;
    this.tags = Tags.of(tags);
  }

  Tag[] withOutboundTags() {
    return toArray(with(tags, direction("outbound")));
  }

  Tag[] withInboundTags() {
    return toArray(with(tags, direction("inbound")));
  }

  Tag[] toTagsArray() {
    return toArray(toList(tags));
  }

  private List<Tag> with(Tags tags, Tag tag) {
    List<Tag> res = toList(tags);
    res.add(tag);
    return res;
  }

  private Tag[] toArray(Collection<Tag> tags) {
    return tags.toArray(new Tag[0]);
  }

  private List<Tag> toList(Iterable<Tag> tags) {
    List<Tag> res = new ArrayList<>();
    tags.forEach(res::add);
    return res;
  }

  private static Tag direction(String direction) {
    return Tag.of("direction", direction);
  }
}
