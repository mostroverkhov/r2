package com.github.mostroverkhov.r2.rxjava.internal

import com.github.mostroverkhov.r2.rxjava.internal.adapters.RequesterAdapter
import com.github.mostroverkhov.r2.core.CoreRequesterBuilder
import io.rsocket.kotlin.RSocket

internal class RequesterBuilder(rSocket: RSocket) : CoreRequesterBuilder() {
    init {
        adapter(RequesterAdapter(rSocket))
    }
}