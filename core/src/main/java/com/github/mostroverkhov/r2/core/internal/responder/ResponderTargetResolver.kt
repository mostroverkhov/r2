package com.github.mostroverkhov.r2.core.internal.responder

import com.github.mostroverkhov.r2.core.DataCodec
import com.github.mostroverkhov.r2.core.Metadata
import com.github.mostroverkhov.r2.core.internal.MetadataCodec
import com.github.mostroverkhov.r2.core.internal.ServiceMethodDecoder
import com.github.mostroverkhov.r2.core.contract.*
import com.github.mostroverkhov.r2.core.internal.RemoteServiceMethod
import com.github.mostroverkhov.r2.core.ServiceReader
import org.reactivestreams.Publisher
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

typealias ArgErr = IllegalArgumentException

class ResponderTargetResolver(private val svcReader: ServiceReader,
                              private val metadataCodec: MetadataCodec,
                              private val serviceMethodDecoder: ServiceMethodDecoder) {
    private val methodResolver = MethodResolver()

    fun resolve(payloadData: ByteBuffer,
                payloadMetadata: ByteBuffer): TargetAction {

        val metadata = decodeMetadata(payloadMetadata)
        val svcMethod = decodeSvcMethod(metadata)

        val svcName = svcMethod.service
        val methodName = svcMethod.method
        val codec = svcMethod.dataCodec

        val service = svcReader[svcName]

        val targetAction: TargetAction = service?.let {
            val method = methodResolver.resolve(it, methodName)
            val args = resolveArguments(method)
            args.setMetadata(metadata)
            args.setRequest { requestType -> decodeData(payloadData, requestType, codec) }
            return TargetAction(service, method, args, codec, svcName, methodName)

        } ?: throw missingServiceError(svcName)

        return targetAction
    }

    private fun decodeSvcMethod(metadata: Metadata): RemoteServiceMethod {
        val svcMethodBuffer = metadata.asByteBuffer().svcMethod()
                ?: throw missingServiceMethodError()
        return serviceMethodDecoder.decode(svcMethodBuffer)
    }

    private fun decodeMetadata(metadata: ByteBuffer): Metadata =
            metadataCodec.decodeForRequest(metadata)

    private fun decodeData(data: ByteBuffer, type: Class<*>, codec: DataCodec): Any
            = codec.decode(data, type)

    private fun missingServiceError(svcName: String) =
            ArgErr("No service with name: $svcName")

    private fun missingServiceMethodError() =
            ArgErr("Request svcMethod is missing")

    internal class MethodResolver {

        private val contractCache = ConcurrentHashMap<Class<*>, Class<*>>()
        private val methodCache = ConcurrentHashMap<String, Method>()

        fun resolve(svc: Any, methodName: String): Method {
            val svcClass = svc.javaClass
            val contractClass = contractCache.getOrPut(svcClass, {
                val contract = resolveSvcContract(svcClass)
                resolveTargetMethods(contract)
                contract
            })

            val key = key(contractClass.name, methodName)
            val method = methodCache[key]
                    ?: throw missingMethodError(svc, methodName)
            return method
        }

        //TODO: verify no duplicate method names
        private fun resolveTargetMethods(svcContractClass: Class<*>) {
            val contractName = svcContractClass.name
            val svcMethods = svcContractClass.declaredMethods

            for (method in svcMethods) {
                for (ann in method.annotations) {
                    /*cant collapse as annotations cant extend interfaces*/
                    when (ann) {
                        is FireAndForget -> cache(contractName, ann.value, method)
                        is RequestResponse -> cache(contractName, ann.value, method)
                        is RequestStream -> cache(contractName, ann.value, method)
                        is RequestChannel -> cache(contractName, ann.value, method)
                    }
                }
            }
        }

        private fun missingMethodError(svc: Any, methodName: String) =
                ArgErr("Service: $svc does not have method: $methodName")

        private fun cache(contractName: String, methodName: String, method: Method) {
            method.isAccessible = true
            methodCache.put(key(contractName, methodName), method)
        }

        private fun key(prefix: String, name: String) = prefix + name
    }

    companion object {

        internal fun resolveSvcContract(service: Class<*>): Class<*> {
            val svcs = service.interfaces.asSequence()
                    .filter { it.getAnnotation(Service::class.java) != null }
                    .toList()
            when {
                svcs.isEmpty() -> throw noContractError(service)
                svcs.size > 1 -> throw multipleContractsError(service)
                else -> return svcs.first()
            }
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

        private fun multipleContractsError(service: Class<*>) =
                ArgErr("Multiple contracts implemented by service ${service.javaClass.name}")

        private fun noContractError(service: Class<*>) =
                ArgErr("No contract implemented by service ${service.javaClass.name}")
    }
}