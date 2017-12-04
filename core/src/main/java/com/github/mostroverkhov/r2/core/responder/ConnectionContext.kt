package com.github.mostroverkhov.r2.core.responder

import com.github.mostroverkhov.r2.core.Metadata

class ConnectionContext(val metadata: Metadata) {

    fun auth(): ByteArray? = metadata.data(Metadata.Keys.AUTH())
}