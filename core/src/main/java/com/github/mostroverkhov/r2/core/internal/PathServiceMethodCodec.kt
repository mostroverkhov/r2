package com.github.mostroverkhov.r2.core.internal

import com.github.mostroverkhov.r2.core.CodecReader
import com.github.mostroverkhov.r2.core.internal.responder.ResponderCall
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

class PathServiceMethodCodec {

    fun encoder() = PathServiceMethodEncoder()

    fun decoder(codecReader: CodecReader) = PathServiceMethodDecoder(codecReader)

    inner class PathServiceMethodDecoder(private val codecReader: CodecReader)
        : ServiceMethodDecoder {

        override fun decode(buffer: ByteBuffer): RemoteServiceMethod =
                asSvcMethod(asString(buffer))

        private fun asSvcMethod(svcMethod: String): RemoteServiceMethod {
            val segments = svcMethod.split("/")
            if (segments.size != 3) {
                throw IllegalArgumentException(
                        "Expected 3 svcMethod segments: $svcMethod")
            }
            val prefix = segments[0]
            return codecReader[prefix]
                    ?.let { codec ->
                        val svc = segments[1]
                        val method = segments[2]
                        ResponderCall(codec, svc, method)
                    }
                    ?: throw IllegalArgumentException(
                            "No codec for prefix: $prefix")
        }

        private fun asString(buffer: ByteBuffer) = charset.decode(buffer).toString()
    }

    inner class PathServiceMethodEncoder : ServiceMethodEncoder {
        override fun encode(serviceMethod: RemoteServiceMethod): ByteBuffer =
                charset.encode(build(serviceMethod))

        private fun build(sm: RemoteServiceMethod) =
                "${sm.dataCodec.prefix}/${sm.service}/${sm.method}"
    }

    companion object {
        private val charset = StandardCharsets.UTF_8
    }
}
