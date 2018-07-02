package com.github.mostroverkhov.r2.reactor.internal;

import com.github.mostroverkhov.r2.core.CoreRequesterBuilder;
import com.github.mostroverkhov.r2.reactor.InteractionsInterceptor;
import com.github.mostroverkhov.r2.reactor.internal.adapters.RequesterAdapter;
import io.rsocket.RSocket;

import java.util.List;

public class RequesterBuilder extends CoreRequesterBuilder {

    public RequesterBuilder(RSocket rSocket, List<InteractionsInterceptor> interceptors) {
        adapter(new RequesterAdapter(rSocket, interceptors));
    }
}
