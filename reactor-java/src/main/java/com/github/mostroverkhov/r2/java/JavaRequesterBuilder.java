package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.RequesterBuilder;
import com.github.mostroverkhov.r2.java.adapters.JavaRequesterAdapter;
import io.rsocket.RSocket;

public class JavaRequesterBuilder extends RequesterBuilder {
    public JavaRequesterBuilder(RSocket rSocket) {
        adapter(new JavaRequesterAdapter(rSocket));
    }
}
