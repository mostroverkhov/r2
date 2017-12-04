package com.github.mostroverkhov.r2.android

import com.github.mostroverkhov.r2.core.requester.RequesterBuilder
import io.rsocket.android.RSocket

class AndroidRequesterBuilder(rSocket: RSocket) : RequesterBuilder() {
    init {
        adapter(AndroidRequesterAdapter(rSocket))
    }
}