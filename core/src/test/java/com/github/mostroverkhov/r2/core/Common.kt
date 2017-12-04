package com.github.mostroverkhov.r2.core

import java.nio.ByteBuffer
import java.nio.charset.Charset

internal fun ByteBuffer.asStr(charset: Charset): String = charset.decode(this).toString()

internal fun String.asBuffer(charset: Charset): ByteBuffer = charset.encode(this)

internal class MockCodec : DataCodec {
    override val prefix: String = "mock"

    override fun <T> encode(obj: T): ByteBuffer {
        throw UnsupportedOperationException()
    }

    override fun <T> decode(buf: ByteBuffer, clazz: Class<T>): T {
        throw UnsupportedOperationException()
    }
}
