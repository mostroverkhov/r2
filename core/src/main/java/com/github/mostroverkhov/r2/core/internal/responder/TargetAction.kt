package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import java.lang.reflect.Method
import java.nio.ByteBuffer

@Suppress("UNCHECKED_CAST")
data class TargetAction(private val target: Any,
                        private val action: Method,
                        private val arg: Any,
                        private val argType: Class<*>,
                        private val codec: DataCodec) {

    operator fun <T> invoke(): T = action(target, arg) as T

    operator fun <T> invoke(arg: Any): T = action(target, arg) as T

    fun encode(data: Any): ByteBuffer = codec.encode(data)

    fun decode(data: ByteBuffer): Any = codec.decode(data, argType)
}
