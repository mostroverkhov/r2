package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.requester.RequesterBuilder;
import com.github.mostroverkhov.rsocket.RSocket;

class JavaRequesterBuilder extends RequesterBuilder {
    JavaRequesterBuilder(RSocket rSocket) {
        adapter(new JavaRequesterAdapter(rSocket));
    }
}
