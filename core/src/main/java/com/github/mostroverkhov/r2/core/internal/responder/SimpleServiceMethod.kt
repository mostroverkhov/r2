package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.internal.ServiceMethod

data class SimpleServiceMethod(override val dataCodec: DataCodec,
                               override val service: String,
                               override val method: String) : ServiceMethod