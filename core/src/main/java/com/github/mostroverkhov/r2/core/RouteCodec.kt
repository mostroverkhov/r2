package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.Route
import java.nio.ByteBuffer

interface RouteEncoder {

    fun encode(route: Route): ByteBuffer
}

interface RouteDecoder {

    fun decode(buffer: ByteBuffer): Route
}
