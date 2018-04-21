package com.github.mostroverkhov.r2.core

import com.github.mostroverkhov.r2.core.contract.Service
import java.util.concurrent.ConcurrentHashMap

class Services : ServiceReader {
    private val services = ConcurrentHashMap<String, Any>()

    fun add(name: String, service: Any): Services {
        if (name.isEmpty()) {
            throw IllegalArgumentException("Service ${service.javaClass.name}: empty name is not allowed")
        }
        services[name] = service
        return this
    }

    fun add(service: Any): Services = add(resolveName(service), service)

    override fun get(name: String): Any? = services[name]

    private fun resolveName(svc: Any): String {
        val ifs = svc.javaClass.interfaces
        val contracts = ifs.asSequence()
                .filter { it.getAnnotation(Service::class.java) != null }
                .toList()

        if (contracts.isEmpty()) {
            throw emptyContractError(svc)
        } else if (contracts.size > 1) {
            throw multipleContractsError(svc)
        } else {
            val svcName = contracts
                    .first()
                    .getAnnotation(Service::class.java).value
            return if (svcName.isEmpty()) {
                throw emptyNameError(svc)
            } else {
                svcName
            }
        }
    }

    private fun emptyNameError(svc: Any) =
            IllegalArgumentException("Service ${svc.javaClass.name}: empty name is not allowed")

    private fun multipleContractsError(svc: Any) =
            IllegalArgumentException("Multiple contracts implemented by service ${svc.javaClass.name}")

    private fun emptyContractError(svc: Any) =
            IllegalArgumentException("No contract implemented by service ${svc.javaClass.name}")
}

interface ServiceReader {

    operator fun get(name: String): Any?
}