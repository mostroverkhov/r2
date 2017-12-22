package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import java.lang.reflect.Method
import java.nio.ByteBuffer
import java.util.*

data class TargetAction internal constructor(private val target: Any,
                                             private val action: Method,
                                             private val args: ActionArgs,
                                             private val codec: DataCodec) {

    @Suppress("UNCHECKED_CAST")
    operator fun <T> invoke(): T = action(target, *args()) as T

    fun request(f: (Any) -> Any): TargetAction {
        args.updateRequest(f)
        return this
    }

    fun encode(data: Any): ByteBuffer = codec.encode(data)

    fun decode(data: ByteBuffer): Any = codec.decode(data, args.requestType())
}

internal class ActionArgs(private val argsSize: Int) {
    private val args = arrayOfNulls<Any>(argsSize)
    private var requestPos: Int = -1
    private var metadataPos: Int = -1
    private var argType: Class<*>? = null

    fun markRequest(pos: Int, argType: Class<*>): ActionArgs {
        requestPos = pos
        this.argType = argType
        return this
    }

    fun markMetadata(pos: Int): ActionArgs {
        metadataPos = pos
        return this
    }

    fun setRequest(value: (Class<*>) -> Any): ActionArgs {
        if (requestPos >= 0) {
            args[requestPos] = value(argType!!)
        }
        return this
    }

    fun setMetadata(value: Metadata): ActionArgs {
        if (metadataPos >= 0) {
            args[metadataPos] = value
        }
        return this
    }

    fun requestType(): Class<*> {
        return if (requestPos < 0) {
            throw AssertionError("Request was not set")
        } else argType!!
    }

    fun updateRequest(f: (Any) -> Any): ActionArgs {
        if (requestPos < 0) {
            throw AssertionError("Trying to update request which was not set")
        } else args[requestPos] = f(args[requestPos]!!)
        return this
    }

    operator fun invoke(): Array<Any?> {
        if (setArgs() == argsSize) {
            return args
        } else {
            throw IllegalStateException("Expected args size $argsSize, args: ${Arrays.toString(args)}")
        }
    }

    internal fun requestPos(): Int? = pos(requestPos)

    internal fun metadataPos(): Int? = pos(metadataPos)

    private fun pos(pos: Int) = if (pos < 0) null else pos

    private fun setArgs(): Int = args.count { it != null }
}
