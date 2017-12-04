package com.github.mostroverkhov.r2.core.internal.requester

import java.lang.reflect.Method

data class TargetAction(val target: Any,
                        val action: Method,
                        val args: Array<out Any>?) {

    operator fun invoke(): Any = action.invoke(target, args)
}


