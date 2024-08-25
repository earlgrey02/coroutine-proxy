package com.kotlinx.proxy.core.jdk

import com.kotlinx.proxy.util.coroutineScope
import com.kotlinx.proxy.util.getContinuation
import com.kotlinx.proxy.util.withoutContinuation
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

abstract class CoInvocationHandler : InvocationHandler {
    final override fun invoke(proxy: Any, method: Method, args: Array<*>?): Any? =
        with(method.kotlinFunction!!) {
            val parameters = args?.toList() ?: emptyList()

            if (isSuspend) {
                parameters.getContinuation<Any?>()
                    .coroutineScope { coInvoke(proxy, this, parameters.withoutContinuation()) }
            } else invoke(proxy, this, parameters)
        }

    abstract suspend fun coInvoke(proxy: Any, function: KFunction<*>, parameters: List<*>): Any?

    abstract fun invoke(proxy: Any, function: KFunction<*>, parameters: List<*>): Any?
}
