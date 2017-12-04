package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.RouteDecoder
import com.github.mostroverkhov.r2.core.responder.ServiceReader
import java.lang.reflect.ParameterizedType
import java.nio.ByteBuffer

class ResponderTargetResolver(private val svcReader: ServiceReader,
                              private val metadataCodec: MetadataCodec,
                              private val routeDecoder: RouteDecoder) {

    fun resolve(payloadData: ByteBuffer,
                payloadMetadata: ByteBuffer): TargetAction {

        val metadata = decodeRoute(payloadMetadata)
        val route = routeDecoder.decode(metadata.asByteBuffer().route()!!)

        val svcName = route.service
        val methodName = route.method

        val service = svcReader[svcName]
        if (service != null) {
            val allMethods = service.javaClass.interfaces[0].declaredMethods
            val targetMethods = allMethods.filter { it.name == methodName }
            if (targetMethods.size > 1) {
                throw IllegalStateException("Multiple methods correspond to $svcName.$methodName")
            }
            if (targetMethods.isEmpty()) {
                throw IllegalStateException("No methods correspond to $svcName.$methodName")
            }
            val targetMethod = targetMethods[0]
            targetMethod.isAccessible = true
            val parameterTypes = targetMethod.genericParameterTypes

            if (parameterTypes.size > 1) {
                throw IllegalStateException("Method $svcName.$methodName has more than 1 argument")
            }
            if (parameterTypes.isEmpty()) {
                throw IllegalStateException("Method $svcName.$methodName has no arguments")
            }
            val paramType = parameterTypes[0]

            val requestType = if (paramType is ParameterizedType) {
                paramType.actualTypeArguments[0]
            } else {
                paramType
            } as Class<*>
            val codec = route.dataCodec
            val payloadT = decodeData(payloadData, requestType, codec)
            return TargetAction(service, targetMethod, payloadT, requestType, codec)
        } else {
            throw IllegalArgumentException("Missing service: $svcName")
        }
    }

    private fun decodeRoute(metadata: ByteBuffer): Metadata =
            metadataCodec.decodeForRequest(metadata)

    private fun decodeData(data: ByteBuffer, type: Class<*>, codec: DataCodec): Any
            = codec.decode(data, type)
}