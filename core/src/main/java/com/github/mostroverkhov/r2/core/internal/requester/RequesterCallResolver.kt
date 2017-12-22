package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.RouteEncoder
import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.requester.Interaction.*
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

internal class RequesterCallResolver(private val dataCodec: DataCodec,
                                     private val metadataCodec: MetadataCodec,
                                     private val routeEncoder: RouteEncoder) {

    fun resolve(targetAction: TargetAction): Call {
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
        throw IllegalArgumentException("$action: No known request interactions amongst " +
                "${action.declaredAnnotations.map { it.annotationClass }}")
    }

    private fun resolveArgs(targetAction: TargetAction,
                            interaction: Interaction): ActionArgs {
        val args = targetAction.args
        if (args != null && args.size > 2) {
            throw IllegalArgumentException("Method: ${targetAction.action.name} of service: " +
                    "${targetAction.target.javaClass} is expected to have at most 2 arguments")
        }
        val builder = ActionArgs.Builder(interaction)
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

    private fun closeCall(interaction: Interaction): CloseCall = CloseCall(interaction)

    private fun requestCall(targetAction: TargetAction,
                            interaction: Interaction,
                            methodName: String): RequestCall {
        assertMethodName(methodName, targetAction)

        val action = targetAction.action
        val serviceName = resolveService(action)
        val responsePayloadType = resolveResponsePayloadType(action)
        val args = resolveArgs(targetAction, interaction)

        return RequestCall(
                routeEncoder,
                metadataCodec,
                dataCodec,
                serviceName,
                methodName,
                args,
                interaction,
                responsePayloadType)
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