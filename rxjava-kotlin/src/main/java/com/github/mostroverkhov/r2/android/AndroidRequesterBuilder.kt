package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.android.adapters.AndroidRequesterAdapter
import com.github.mostroverkhov.r2.core.RequesterBuilder
import io.rsocket.android.RSocket

internal class AndroidRequesterBuilder(rSocket: RSocket) : RequesterBuilder() {
    init {
        adapter(AndroidRequesterAdapter(rSocket))
    }
}