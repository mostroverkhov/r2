package com.github.mostroverkhov.r2.core.internal

import com.github.mostroverkhov.r2.core.RouteDecoder
import com.github.mostroverkhov.r2.core.RouteEncoder
import com.github.mostroverkhov.r2.core.responder.CodecReader
import com.github.mostroverkhov.r2.core.internal.responder.SimpleRoute
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class StringRouteCodec {

    fun encoder() = StringRouteEncoder()

    fun decoder(codecReader: CodecReader) = StringRouteDecoder(codecReader)

    inner class StringRouteDecoder(private val codecReader: CodecReader) : RouteDecoder {

        override fun decode(buffer: ByteBuffer): Route = asRoute(asString(buffer))

        private fun asRoute(route: String): Route {
            val segments = route.split("/")
            if (segments.size != 3) {
                throw IllegalArgumentException("Expected 3 route segments: $route")
            }
            val prefix = segments[0]
            return codecReader[prefix]
                    ?.let { codec ->
                        val svc = segments[1]
                        val method = segments[2]
                        SimpleRoute(codec, svc, method)
                    } ?: throw IllegalArgumentException("No codec for prefix: $prefix")
        }

        private fun asString(buffer: ByteBuffer) = charset.decode(buffer).toString()
    }

    inner class StringRouteEncoder : RouteEncoder {
        override fun encode(route: Route): ByteBuffer = charset.encode(buildPath(route))

        private fun buildPath(route: Route): String
                = "${route.dataCodec.prefix}/${route.service}/${route.method}"
    }

    companion object {
        private val charset = StandardCharsets.UTF_8
    }
}
