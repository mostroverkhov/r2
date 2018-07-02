package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.PathServiceMethodCodec
import com.github.mostroverkhov.r2.core.internal.responder.ResponderCall
import org.junit.Assert.assertEquals
import org.junit.Test

class PathServiceMethodCodecTest {

    private val svcMethodCodec = PathServiceMethodCodec()
    private val dataCodec = MockCodec()
    private var charset = Charsets.UTF_8
    private val expectedPath = "mock/svc/method"
    private val expectedPathBuffer = expectedPath.asBuffer(charset)

    @Test
    fun encode() {
        val encoded = svcMethodCodec.encoder()
                .encode(ResponderCall(dataCodec, "svc", "method"))
        val encodedPath = encoded.asStr(charset)
        assertEquals("mock/svc/method", encodedPath)
    }

    @Test
    fun decode() {
        val decoded = svcMethodCodec.decoder(Codecs().add(dataCodec))
                .decode(expectedPathBuffer)

        assertEquals("mock", decoded.dataCodec.prefix)
        assertEquals("svc", decoded.service)
        assertEquals("method", decoded.method)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeMissingCodec() {
        svcMethodCodec.decoder(Codecs())
                .decode(expectedPathBuffer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWrongPath() {
        svcMethodCodec.decoder(Codecs())
                .decode("foo/bar".asBuffer(charset))
    }
}