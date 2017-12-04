package com.github.mostroverkhov.r2.core

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer

class MetadataBuilderTest {
    val charset = Charsets.UTF_8

    @Test
    fun auth() {
        val metadata = Metadata.Builder().auth("foo".asBytes()).build()
        assertEquals(1, metadata.keys().size)
        assertEquals("foo", metadata.auth()!!.asString())

        assertFalse(metadata.hasRoute())
        assertTrue(metadata.route() == null)

    }

    @Test
    fun key() {
        val metadata = Metadata.Builder().data("key", "foo".asBytes()).build()
        assertEquals(1, metadata.keys().size)
        assertEquals("foo", metadata.data("key")!!.asString())
        assertTrue(metadata.auth() == null)
    }

    @Test
    fun requestMetadata() {
        val metadata = Metadata.RequestBuilder()
                .route(ByteBuffer.allocate(42))
                .data("key", "foo".asBytes())
                .build()
        assertEquals(1, metadata.keys().size)
        assertEquals("foo", metadata.data("key")!!.asString())
        assertTrue(metadata.hasRoute())
        assertTrue(metadata.route() != null)

    }

    @Test(expected = Exception::class)
    fun requestMetadataNoRoute() {
        Metadata.RequestBuilder()
                .data("key", "foo".asBytes())
                .build()
    }


    private fun String.asBytes() = toByteArray(charset)

    private fun ByteArray.asString() = String(this, charset)
}