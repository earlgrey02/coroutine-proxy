package com.kotlinx.proxy.util

import kotlin.coroutines.Continuation

internal fun <T> List<*>.getContinuation(): Continuation<T> = last { it is Continuation<*> } as Continuation<T>

internal fun List<*>.withoutContinuation(): List<*> = take(size - 1)
