package com.github.mostroverkhov.r2.core

class ConnectionContext(val metadata: Metadata) {

    fun auth(): ByteArray? = metadata.data(Metadata.Keys.AUTH())
}