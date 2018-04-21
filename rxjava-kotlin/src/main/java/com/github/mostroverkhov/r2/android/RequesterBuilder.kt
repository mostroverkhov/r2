package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.RequesterAdapter
import com.github.mostroverkhov.r2.core.CoreRequesterBuilder
import io.rsocket.android.RSocket

internal class RequesterBuilder(rSocket: RSocket) : CoreRequesterBuilder() {
    init {
        adapter(RequesterAdapter(rSocket))
    }
}