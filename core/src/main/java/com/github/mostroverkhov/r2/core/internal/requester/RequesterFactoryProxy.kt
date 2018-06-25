package com.github.mostroverkhov.r2.core.internal.requester

import com.github.mostroverkhov.r2.core.RequesterFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


internal class RequesterFactoryProxy constructor(private val adapter: CallAdapter,
                                                 private val callResolver: RequesterCallResolver)
    : RequesterFactory {

    override fun <T> create(clazz: Class<T>): T = CallHandler(callResolver, clazz).handleWith(adapter)

    /*introduced for testing*/
    internal class CallHandler<T>(private val callResolver: RequesterCallResolver,
                                  private val clazz: Class<T>) {
        @Suppress("UNCHECKED_CAST")
        fun handleWith(callAdapter: CallAdapter): T = Proxy.newProxyInstance(
                CallHandler::class.java.classLoader,
                arrayOf(clazz),
                Handler(callAdapter)) as T

        private inner class Handler(private val callAdapter: CallAdapter) : InvocationHandler {

            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                return if (method.declaringClass === Any::class.java)
                    method.invoke(proxy, args)
                else {
                    try {
                        callAdapter.adapt(callResolver.resolve(TargetAction(proxy, method, args)))
                    } catch (e: RuntimeException) {
                        callAdapter.resolve(method, e)
                    }
                }
            }
        }
    }
}