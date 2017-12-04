package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.*
import com.github.mostroverkhov.r2.core.internal.requester.Interaction.*
import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.Route
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

internal class RequesterCallResolver(private val dataCodec: DataCodec,
                                     private val metadataCodec: MetadataCodec,
                                     private val routeEncoder: RouteEncoder,
                                     private val metadataBuilder: Metadata.RequestBuilder) {

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

    private fun metadataFactory(): (Route) -> Metadata {
        return { route: Route ->
            val encodedRoute = routeEncoder.encode(route)
            metadataBuilder.route(encodedRoute).build()
        }
    }

    private fun resolveArg(targetAction: TargetAction): Any {
        val args = targetAction.args
        if (args == null || args.size != 1) {
            throw IllegalArgumentException("Method: ${targetAction.action.name} of service: " +
                    "${targetAction.target.javaClass} is expected to have 1 argument")
        }
        return args[0]
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

    private fun closeCall(interaction: Interaction): CloseCall = CloseCall(interaction)

    private fun requestCall(targetAction: TargetAction,
                            methodInteraction: Interaction,
                            methodName: String): RequestCall {

        assertMethodName(methodName, targetAction)

        val action = targetAction.action

        val serviceName = resolveService(action)
        val responsePayloadType = resolveResponsePayloadType(action)
        val arg = resolveArg(targetAction)

        return RequestCall(
                metadataFactory(),
                metadataCodec,
                dataCodec,
                serviceName,
                methodName,
                arg,
                methodInteraction,
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

    private fun resolveResponsePayloadType(m: Method): Class<*> {
        val returnType = m.genericReturnType
        return if (returnType is ParameterizedType) {
            return returnType.actualTypeArguments[0] as Class<*>
        } else {
            Nothing::class.java
        }
    }
}