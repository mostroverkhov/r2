package com.github.mostroverkhov.r2.core

import java.nio.ByteBuffer

interface DataCodec {

    val prefix: String

    fun <T> encode(obj: T): ByteBuffer

    fun <T> decode(buf: ByteBuffer, clazz: Class<T>): T
}