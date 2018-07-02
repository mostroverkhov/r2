package com.github.mostroverkhov.r2.core.internal

import com.github.mostroverkhov.r2.core.DataCodec

interface RemoteServiceMethod : ServiceMethod {

    val dataCodec: DataCodec
}