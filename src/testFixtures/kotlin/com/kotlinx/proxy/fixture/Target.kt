package com.kotlinx.proxy.fixture

interface Target {
    suspend fun `1초 후 1을 반환하는 메서드`(): Int

    fun `1을 반환하는 메서드`(): Int
}
