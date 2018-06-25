package com.github.mostroverkhov.r2.reactor;

import java.util.ArrayList;
import java.util.List;

public class InterceptorOptions {
  private final List<InteractionsInterceptor> requesterInterceptors = new ArrayList<>();
  private final List<InteractionsInterceptor> handlerInterceptors = new ArrayList<>();

  public InterceptorOptions requester(InteractionsInterceptor interceptor) {
    requesterInterceptors.add(interceptor);
    return this;
  }

  public InterceptorOptions handler(InteractionsInterceptor interceptor) {
    handlerInterceptors.add(interceptor);
    return this;
  }

  public List<InteractionsInterceptor> requesters() {
    return new ArrayList<>(requesterInterceptors);
  }

  public List<InteractionsInterceptor> handlers() {
    return new ArrayList<>(handlerInterceptors);
  }
}
