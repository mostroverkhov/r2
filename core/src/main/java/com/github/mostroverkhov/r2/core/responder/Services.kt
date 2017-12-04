package com.github.mostroverkhov.r2.core.responder

import com.github.mostroverkhov.r2.core.contract.Service
import java.util.concurrent.ConcurrentHashMap

class Services : ServiceReader {
    private val services = ConcurrentHashMap<String, Any>()

    fun add(name: String, service: Any): Services {
        services[name] = service
        return this
    }

    fun add(service: Any): Services {
        val name = resolveName(service)
        if (name.isEmpty()) {
            throw IllegalArgumentException("Cant resolve name of service ${service.javaClass}")
        } else {
            return add(name, service)
        }
    }

    override fun get(name: String): Any? = services[name]

    private fun resolveName(svc: Any): String {
        val ifs = svc.javaClass.interfaces
        return ifs.firstOrNull { it.isAnnotationPresent(Service::class.java) }
                ?.getAnnotation(Service::class.java)?.value ?: ""
    }
}

interface ServiceReader {

    operator fun get(name: String): Any?
}