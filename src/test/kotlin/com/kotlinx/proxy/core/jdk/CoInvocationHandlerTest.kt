package com.kotlinx.proxy.core.jdk

import com.kotlinx.proxy.fixture.Target
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldNotBeEqual
import kotlinx.coroutines.delay
import java.lang.reflect.Proxy
import kotlin.reflect.KFunction

class CoInvocationHandlerTest : BehaviorSpec() {
    init {
        Given("프록시를 적용한 Target은") {
            val handler = object : CoInvocationHandler() {
                override suspend fun coInvoke(proxy: Any, function: KFunction<*>, parameters: List<*>): Any =
                    2.apply {
                        delay(1000)
                    }

                override fun invoke(proxy: Any, function: KFunction<*>, parameters: List<*>): Any = 2
            }
            val target = Proxy.newProxyInstance(
                Target::class.java.classLoader,
                arrayOf(Target::class.java),
                handler
            ) as Target

            When("1초 후 1을 반환하는 메서드를 호출하면") {
                val result = target.`1초 후 1을 반환하는 메서드`()

                Then("1초 후 1이 아닌 값을 반환한다.") {
                    result shouldNotBeEqual 1
                }
            }

            When("1을 반환하는 메서드를 호출하면") {
                val result = target.`1을 반환하는 메서드`()

                Then("바로 1이 아닌 값을 반환한다.") {
                    result shouldNotBeEqual 1
                }
            }
        }
    }
}
