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
    fun svcMethod() {
        val svcMethod = "proto/1/svc/method"
        assertSvcMethodWriteRead(svcMethod, mapOf("foo" to "bar"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun svcMethodExceedsLength() {
        val svcMethod = "proto/1/svc/method".times(4000)
        assertSvcMethodWriteRead(svcMethod, mapOf("foo" to "bar"))
    }

    @Test()
    fun longKey() {
        val svcMethod = "proto/1/svc/method"
        assertSvcMethodWriteRead(svcMethod, mapOf("foo".times(50) to "bar"))
    }

    private fun assertSvcMethodWriteRead(svcMethod: String, keyValues: Map<String, String>) {
        val builder = Metadata.RequestBuilder().svcMethod(encode(svcMethod))

        keyValues.forEach { k, v ->
            builder.data(k, charset.encode(v))
        }
        val metadata = builder.build()
        val byteBuffer = metadataCodec.encode(metadata)
        val decodedMetadata = metadataCodec.decodeForRequest(byteBuffer)

        assertEq(svcMethod, keyValues, decodedMetadata)
    }

    private fun assertEq(svcMethod: String, keyValues: Map<String, String>, decodedMetadata: Metadata) {
        assertEquals(svcMethod, String(decodedMetadata.svcMethod()!!, charset))
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

    private fun encode(svcMethod: String) = charset.encode(svcMethod)
}