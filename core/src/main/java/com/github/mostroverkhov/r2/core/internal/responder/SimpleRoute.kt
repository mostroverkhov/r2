package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.internal.Route

data class SimpleRoute(override val dataCodec: DataCodec,
                       override val service: String,
                       override val method: String) : Route