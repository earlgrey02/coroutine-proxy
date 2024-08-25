package com.kotlinx.proxy.fixture

import kotlinx.coroutines.delay

open class TargetImpl : Target {
    override suspend fun `1초 후 1을 반환하는 메서드`(): Int =
        1.apply {
            delay(1000)
        }

    override fun `1을 반환하는 메서드`(): Int = 1
}
