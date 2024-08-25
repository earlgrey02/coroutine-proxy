package com.kotlinx.proxy.core.cglib

import com.kotlinx.proxy.util.coroutineScope
import com.kotlinx.proxy.util.getContinuation
import com.kotlinx.proxy.util.withoutContinuation
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

abstract class CoMethodInterceptor : MethodInterceptor {
    final override fun intercept(proxy: Any, method: Method, args: Array<*>, methodProxy: MethodProxy): Any? =
        with(method.kotlinFunction!!) {
            val parameters = args.toList()

            if (isSuspend) {
                parameters.getContinuation<Any?>()
                    .coroutineScope { coIntercept(proxy, this, parameters.withoutContinuation(), methodProxy) }
            } else intercept(proxy, this, parameters, methodProxy)
        }

    abstract suspend fun coIntercept(
        proxy: Any, function: KFunction<*>, parameters: List<*>, methodProxy: MethodProxy
    ): Any?

    abstract fun intercept(proxy: Any, function: KFunction<*>, parameters: List<*>, methodProxy: MethodProxy): Any?
}
