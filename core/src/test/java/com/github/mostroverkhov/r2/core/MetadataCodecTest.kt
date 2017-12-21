package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MetadataCodecTest {
    private lateinit var metadataCodec: MetadataCodec
    private val charset = Charsets.UTF_8

    @Before
    fun setUp() {
        metadataCodec = MetadataCodec()
    }

    @Test
    fun route() {
        val route = "proto/1/svc/method"
        assertRouteWriteRead(route, mapOf("foo" to "bar"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun routeExceedsLength() {
        val route = "proto/1/svc/method".times(4000)
        assertRouteWriteRead(route, mapOf("foo" to "bar"))
    }

    @Test()
    fun longKey() {
        val route = "proto/1/svc/method"
        assertRouteWriteRead(route, mapOf("foo".times(50) to "bar"))
    }

    private fun assertRouteWriteRead(route: String, keyValues: Map<String, String>) {
        val builder = Metadata.RequestBuilder().route(encode(route))

        keyValues.forEach { k, v ->
            builder.data(k, charset.encode(v))
        }
        val metadata = builder.build()
        val byteBuffer = metadataCodec.encode(metadata)
        val decodedMetadata = metadataCodec.decodeForRequest(byteBuffer)

        assertEq(route, keyValues, decodedMetadata)
    }

    private fun assertEq(route: String, keyValues: Map<String, String>, decodedMetadata: Metadata) {
        assertEquals(route, String(decodedMetadata.route()!!, charset))
        val decodedKeys = decodedMetadata.keys()
        assertEquals(keyValues.keys, decodedKeys)

        decodedKeys.forEach { k ->
            assertTrue(keyValues.contains(k))
            val decodedV = decodedMetadata.data(k)
            val v = keyValues[k]!!.toByteArray(charset)
            assertArrayEquals(v, decodedV)
        }
    }

    private fun String.times(n: Int): String {

        tailrec fun String.times(b: StringBuilder, n: Int): String {
            return if (n > 0) times(b.append(this), n - 1) else b.toString()
        }
        return times(StringBuilder(), n)
    }

    private fun encode(route: String) = charset.encode(route)
}