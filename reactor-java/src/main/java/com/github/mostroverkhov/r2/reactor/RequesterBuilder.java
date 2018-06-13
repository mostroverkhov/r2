package com.github.mostroverkhov.r2.reactor;

import com.github.mostroverkhov.r2.core.CoreRequesterBuilder;
import com.github.mostroverkhov.r2.reactor.adapters.RequesterAdapter;
import io.rsocket.RSocket;

public class RequesterBuilder extends CoreRequesterBuilder {
    public RequesterBuilder(RSocket rSocket) {
        adapter(new RequesterAdapter(rSocket));
    }
}
