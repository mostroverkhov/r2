package com.github.mostroverkhov.r2.core.requester

import com.github.mostroverkhov.r2.core.internal.requester.Call
import com.github.mostroverkhov.r2.core.internal.requester.CallAdapter
import com.github.mostroverkhov.r2.core.internal.requester.RequesterCallResolver
import com.github.mostroverkhov.r2.core.internal.requester.TargetAction
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


class RequesterFactory internal constructor(private val adapter: CallAdapter,
                                            private val callResolver: RequesterCallResolver) {

    fun <T> create(clazz: Class<T>): T = CallHandler(callResolver, clazz).handleWith { adapter.adapt(it) }

    /*introduced for testing*/
    internal class CallHandler<T>(private val callResolver: RequesterCallResolver,
                                  private val clazz: Class<T>) {
        @Suppress("UNCHECKED_CAST")
        fun handleWith(callHandler: (Call) -> Any): T = Proxy.newProxyInstance(
                CallHandler::class.java.classLoader,
                arrayOf(clazz),
                Handler(callHandler)) as T

        private inner class Handler(private val callAdapter: (Call) -> Any) : InvocationHandler {

            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                return if (method.declaringClass === Any::class.java)
                    method.invoke(proxy, args)
                else {
                    callAdapter(callResolver.resolve(TargetAction(proxy, method, args)))
                }
            }
        }
    }
}