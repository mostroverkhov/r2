package com.github.mostroverkhov.r2.core.internal.responder

interface RequestAcceptor<SetupPayload, HandlerRSocket> {

    fun accept(setup: SetupPayload): HandlerRSocket
}