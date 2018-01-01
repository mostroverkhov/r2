package com.github.mostroverkhov.r2.core.internal.requester

import java.lang.reflect.Method

interface CallAdapter {

    fun adapt(call: Call): Any

    fun resolve(action: Method, err: RuntimeException): Any
}