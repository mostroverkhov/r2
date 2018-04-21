package com.github.mostroverkhov.r2.java;

import com.github.mostroverkhov.r2.core.CoreRequesterBuilder;
import com.github.mostroverkhov.r2.java.adapters.RequesterAdapter;
import io.rsocket.RSocket;

public class RequesterBuilder extends CoreRequesterBuilder {
    public RequesterBuilder(RSocket rSocket) {
        adapter(new RequesterAdapter(rSocket));
    }
}
