package com.github.mostroverkhov.r2.core.internal.requester

interface CallAdapter {

    fun adapt(call: Call): Any
}