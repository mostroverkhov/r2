package com.github.mostroverkhov.r2.core.internal

import com.github.mostroverkhov.r2.core.DataCodec

interface Route {

    val dataCodec: DataCodec

    val service: String

    val method: String
}