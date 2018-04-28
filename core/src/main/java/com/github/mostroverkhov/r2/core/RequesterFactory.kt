package com.github.mostroverkhov.r2.core

interface RequesterFactory {

    fun <T> create(clazz: Class<T>): T
}