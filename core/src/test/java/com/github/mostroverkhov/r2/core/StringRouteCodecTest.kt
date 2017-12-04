package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.StringRouteCodec
import com.github.mostroverkhov.r2.core.internal.responder.SimpleRoute
import com.github.mostroverkhov.r2.core.responder.Codecs
import org.junit.Assert.assertEquals
import org.junit.Test

class StringRouteCodecTest {

    private val routeCodec = StringRouteCodec()
    private val dataCodec = MockCodec()
    private var charset = Charsets.UTF_8
    private val expectedPath = "mock/svc/method"
    private val expectedPathBuffer = expectedPath.asBuffer(charset)

    @Test
    fun encode() {
        val encoded = routeCodec.encoder()
                .encode(SimpleRoute(dataCodec, "svc", "method"))
        val encodedPath = encoded.asStr(charset)
        assertEquals("mock/svc/method", encodedPath)
    }

    @Test
    fun decode() {
        val decoded = routeCodec.decoder(Codecs().add(dataCodec))
                .decode(expectedPathBuffer)

        assertEquals("mock", decoded.dataCodec.prefix)
        assertEquals("svc", decoded.service)
        assertEquals("method", decoded.method)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeMissingCodec() {
        val decoded = routeCodec.decoder(Codecs())
                .decode(expectedPathBuffer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeWrongPath() {
        val decoded = routeCodec.decoder(Codecs())
                .decode("foo/bar".asBuffer(charset))
    }
}