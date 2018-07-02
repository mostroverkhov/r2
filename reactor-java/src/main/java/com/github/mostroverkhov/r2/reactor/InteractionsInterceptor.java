package com.github.mostroverkhov.r2.reactor;

import java.util.function.Function;

@FunctionalInterface
public interface InteractionsInterceptor extends Function<Interactions, Interactions> {}
