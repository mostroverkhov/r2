package com.github.mostroverkhov.r2.core.internal.acceptor

interface ClientAcceptor<RequesterRSocket, HandlerRSocket> {

    fun accept(requester: RequesterRSocket): HandlerRSocket
}