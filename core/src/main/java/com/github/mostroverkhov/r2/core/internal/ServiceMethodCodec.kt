package com.github.mostroverkhov.r2.core.internal

import java.nio.ByteBuffer

interface ServiceMethodEncoder {

    fun encode(serviceMethod: RemoteServiceMethod): ByteBuffer
}

interface ServiceMethodDecoder {

    fun decode(buffer: ByteBuffer): RemoteServiceMethod
}
