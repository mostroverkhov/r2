package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.RouteDecoder
import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.responder.ServiceReader
import org.reactivestreams.Publisher
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.ByteBuffer

typealias ArgErr = IllegalArgumentException

class ResponderTargetResolver(private val svcReader: ServiceReader,
                              private val metadataCodec: MetadataCodec,
                              private val routeDecoder: RouteDecoder) {

    fun resolve(payloadData: ByteBuffer,
                payloadMetadata: ByteBuffer): TargetAction {

        val metadata = decodeMetadata(payloadMetadata)
        val routeBuffer = metadata.asByteBuffer().route()
                ?: throw ArgErr("Request route is missing")
        val route = routeDecoder.decode(routeBuffer)

        val svcName = route.service
        val methodName = route.method
        val codec = route.dataCodec

        val service = svcReader[svcName]

        val targetAction: TargetAction = service?.let {
            val svcContract = resolveSvcContract(it)
            val targetMethod = resolveTargetMethod(svcContract, methodName)
            val args = resolveArguments(targetMethod)
            args.setMetadata(metadata)
            args.setRequest { requestType -> decodeData(payloadData, requestType, codec) }

            return TargetAction(service, targetMethod, args, codec)

        } ?: throw ArgErr("No handler with name: $svcName")

        return targetAction
    }

    private fun decodeMetadata(metadata: ByteBuffer): Metadata =
            metadataCodec.decodeForRequest(metadata)

    private fun decodeData(data: ByteBuffer, type: Class<*>, codec: DataCodec): Any
            = codec.decode(data, type)

    companion object {

        internal fun resolveSvcContract(service: Any): Class<*> {
            return service.javaClass.interfaces.asSequence()
                    .filter { it.getAnnotation(Service::class.java) != null }
                    .firstOrNull()
                    ?: throw ArgErr("No contract implemented by service ${service.javaClass.name}")
        }

        internal fun resolveTargetMethod(svcContract: Class<*>,
                                         methodName: String): Method {
            val svcMethods = svcContract.declaredMethods
            val targetMethods = svcMethods.filter { method ->
                for (ann in method.annotations) {
                    val isTargetName = when (ann) {
                        is FireAndForget -> ann.value == methodName
                        is RequestResponse -> ann.value == methodName
                        is RequestStream -> ann.value == methodName
                        is RequestChannel -> ann.value == methodName
                        else -> false
                    }
                    if (isTargetName) return@filter true
                }
                return@filter false
            }

            assertTargetMethods(targetMethods, svcContract, methodName)

            val targetMethod = targetMethods[0]
            targetMethod.isAccessible = true
            return targetMethod
        }

        internal fun resolveArguments(targetMethod: Method): ActionArgs {
            val parameterTypes = targetMethod.genericParameterTypes
            val paramsCount = parameterTypes.size
            assertParamsCount(paramsCount, targetMethod)

            val actionArgs = ActionArgs(paramsCount)

            for (pos in 0 until paramsCount) {

                val paramType = parameterTypes[pos]
                if (isMetadata(paramType)) {
                    actionArgs.markMetadata(pos)
                } else {
                    val requestArgType = if (isPublisher(paramType)) {
                        publisherType(paramType)
                    } else {
                        paramType
                    } as Class<*>

                    actionArgs.markRequest(pos, requestArgType)
                }
            }
            return actionArgs
        }

        private fun isMetadata(paramType: Type?) = paramType == Metadata::class.java

        private fun publisherType(paramType: Type?): Type {
            paramType as ParameterizedType
            return paramType.actualTypeArguments[0]
        }

        private fun isPublisher(paramType: Type?) =
                paramType is ParameterizedType &&
                        Publisher::class.java.isAssignableFrom(paramType.rawType as Class<*>)

        private fun assertParamsCount(paramsCount: Int, targetMethod: Method) {
            if (paramsCount > 2) {
                throw ArgErr("Handler method $targetMethod has more than 2 arguments")
            }
        }

        private fun assertTargetMethods(targetMethods: List<Method>, svcContract: Class<*>, methodName: String) {
            if (targetMethods.size > 1) {
                throw ArgErr("${svcContract.name}: multiple methods correspond to $methodName")
            }
            if (targetMethods.isEmpty()) {
                throw ArgErr("${svcContract.name} :no methods correspond to $methodName")
            }
        }
    }
}