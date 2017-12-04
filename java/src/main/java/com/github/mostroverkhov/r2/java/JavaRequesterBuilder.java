package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.requester.RequesterBuilder;
import io.rsocket.RSocket;

class JavaRequesterBuilder extends RequesterBuilder {
    public JavaRequesterBuilder(RSocket rSocket) {
        adapter(new JavaRequesterAdapter(rSocket));
    }
}
