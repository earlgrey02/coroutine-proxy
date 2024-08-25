package com.kotlinx.proxy.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

internal fun <T> Continuation<T>.coroutineScope(block: suspend () -> T): Any =
    with(CoroutineScope(context)) {
        launch {
            runCatching { block() }
                .run(::resumeWith)
        }

        COROUTINE_SUSPENDED
    }
