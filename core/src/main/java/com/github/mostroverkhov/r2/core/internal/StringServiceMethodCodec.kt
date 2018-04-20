package com.github.mostroverkhov.r2.core.internal

import com.github.mostroverkhov.r2.core.CodecReader
import com.github.mostroverkhov.r2.core.internal.responder.SimpleServiceMethod
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class StringServiceMethodCodec {

    fun encoder() = StringServiceMethodEncoder()

    fun decoder(codecReader: CodecReader) = StringServiceMethodDecoder(codecReader)

    inner class StringServiceMethodDecoder(private val codecReader: CodecReader) : ServiceMethodDecoder {

        override fun decode(buffer: ByteBuffer): ServiceMethod = asRoute(asString(buffer))

        private fun asRoute(route: String): ServiceMethod {
            val segments = route.split("/")
            if (segments.size != 3) {
                throw IllegalArgumentException("Expected 3 route segments: $route")
            }
            val prefix = segments[0]
            return codecReader[prefix]
                    ?.let { codec ->
                        val svc = segments[1]
                        val method = segments[2]
                        SimpleServiceMethod(codec, svc, method)
                    } ?: throw IllegalArgumentException("No codec for prefix: $prefix")
        }

        private fun asString(buffer: ByteBuffer) = charset.decode(buffer).toString()
    }

    inner class StringServiceMethodEncoder : ServiceMethodEncoder {
        override fun encode(serviceMethod: ServiceMethod): ByteBuffer = charset.encode(buildPath(serviceMethod))

        private fun buildPath(serviceMethod: ServiceMethod): String
                = "${serviceMethod.dataCodec.prefix}/${serviceMethod.service}/${serviceMethod.method}"
    }

    companion object {
        private val charset = StandardCharsets.UTF_8
    }
}
