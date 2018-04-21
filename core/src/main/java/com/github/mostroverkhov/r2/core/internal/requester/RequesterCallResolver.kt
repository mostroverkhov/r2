package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.ServiceMethodEncoder
import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.requester.Interaction.*
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.util.concurrent.ConcurrentHashMap

internal class RequesterCallResolver(private val dataCodec: DataCodec,
                                     private val metadataCodec: MetadataCodec,
                                     private val routeEncoder: ServiceMethodEncoder) {

    private val requestCallCache = ConcurrentHashMap<Method, () -> RequestCall>()

    fun resolve(targetAction: TargetAction): Call {
        val call = resolveFastPath(targetAction)
                ?: resolveSlowPath(targetAction)
                ?: throw unknownInteraction(targetAction)
        return when (call) {
            is RequestCall -> call.setArgs(resolveArgs(targetAction, call))
            else -> call
        }
    }

    private fun resolveFastPath(targetAction: TargetAction): Call? {
        return requestCallCache[targetAction.action]?.let { it() } ?: return null
    }

    private fun resolveSlowPath(targetAction: TargetAction): Call? {
        val action = targetAction.action
        for (ann in action.declaredAnnotations) {
            val call = when (ann) {
                is FireAndForget -> requestCall(targetAction, FNF, ann.value)
                is RequestResponse -> requestCall(targetAction, RESPONSE, ann.value)
                is RequestStream -> requestCall(targetAction, STREAM, ann.value)
                is RequestChannel -> requestCall(targetAction, CHANNEL, ann.value)
                is Close -> closeCall(CLOSE)
                is OnClose -> closeCall(ONCLOSE)
                else -> null
            }
            call?.let { return it }
        }
        return null
    }

    private fun unknownInteraction(targetAction: TargetAction): IllegalArgumentException {
        val action = targetAction.action
        return IllegalArgumentException("$action: No known request interactions amongst " +
                "${action.declaredAnnotations.map { it.annotationClass }}")
    }

    private fun resolveArgs(targetAction: TargetAction,
                            call: Call): ActionArgs {
        val args = targetAction.args
        if (args != null && args.size > 2) {
            throw IllegalArgumentException("Method: ${targetAction.action.name} of service: " +
                    "${targetAction.target.javaClass} is expected to have at most 2 arguments")
        }
        val builder = ActionArgs.Builder(call.interaction)
        args?.forEach {
            when (it) {
                is Metadata -> builder.metadata(it)
                else -> builder.data(it)
            }
        }
        return builder.build()
    }

    private fun resolveService(m: Method): String {
        val svcClass = m.declaringClass
        val svcName = svcClass.getAnnotation(Service::class.java)?.value
        if (svcName.isNullOrEmpty()) {
            throw IllegalArgumentException("Service ${svcClass.name} name should not be null or empty")
        } else {
            return svcName!!
        }
    }

    private fun resolveResponsePayloadType(m: Method): Class<*> {
        val returnType = m.genericReturnType
        return if (returnType is ParameterizedType) {
            val returnTypeArgs = returnType.actualTypeArguments
            if (returnTypeArgs.size != 1) {
                throw IllegalArgumentException("$m: generic return type is expected to have 1 type argument")
            }
            return returnTypeArgs[0] as Class<*>
        } else {
            Nothing::class.java
        }
    }

    private fun closeCall(interaction: Interaction) = CloseCall(interaction)

    private fun requestCall(targetAction: TargetAction,
                            interaction: Interaction,
                            methodName: String): Call {
        assertMethodName(methodName, targetAction)

        val action = targetAction.action
        val callProducer = requestCallCache.getOrPut(
                action,
                { requestCallProducer(action, interaction, methodName) })
        return callProducer()
    }

    private fun requestCallProducer(action: Method,
                                    interaction: Interaction,
                                    methodName: String): () -> RequestCall {
        val serviceName = resolveService(action)
        val responsePayloadType = resolveResponsePayloadType(action)

        return {
            RequestCall(
                    routeEncoder,
                    metadataCodec,
                    dataCodec,
                    serviceName,
                    methodName,
                    interaction,
                    responsePayloadType)
        }
    }

    private fun assertMethodName(methodName: String,
                                 targetAction: TargetAction) {
        if (methodName.isEmpty()) {
            val action = targetAction.action
            throw IllegalArgumentException("Call ${action.declaringClass.name}." +
                    "${action.name} has empty method name")
        }
    }
}