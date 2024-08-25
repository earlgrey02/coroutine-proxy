package com.kotlinx.proxy.core.cglib

import com.kotlinx.proxy.fixture.Target
import com.kotlinx.proxy.fixture.TargetImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldNotBeEqual
import kotlinx.coroutines.delay
import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodProxy
import kotlin.reflect.KFunction

class CoMethodInterceptorTest : BehaviorSpec() {
    init {
        Given("프록시를 적용한 Target은") {
            val interceptor = object : CoMethodInterceptor() {
                override suspend fun coIntercept(
                    proxy: Any,
                    function: KFunction<*>,
                    parameters: List<*>,
                    methodProxy: MethodProxy
                ): Any =
                    2.apply {
                        delay(1000)
                    }

                override fun intercept(
                    proxy: Any,
                    function: KFunction<*>,
                    parameters: List<*>,
                    methodProxy: MethodProxy
                ): Any = 2
            }
            val target = Enhancer.create(
                TargetImpl::class.java,
                interceptor
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
