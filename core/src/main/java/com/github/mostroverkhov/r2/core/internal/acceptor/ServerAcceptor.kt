package com.github.mostroverkhov.r2.core.internal.acceptor

interface ServerAcceptor<SetupPayload, RequesterRSocket, HandlerRSocket> {

    fun accept(setup: SetupPayload, rSocket: RequesterRSocket): HandlerRSocket
}