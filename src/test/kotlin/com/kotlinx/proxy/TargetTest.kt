package com.kotlinx.proxy

import com.kotlinx.proxy.fixture.TargetImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual

class TargetTest : BehaviorSpec() {
    init {
        Given("프록시를 적용하지 않은 Target은") {
            val target = TargetImpl()

            When("1초 후 1을 반환하는 메서드를 호출하면") {
                val result = target.`1초 후 1을 반환하는 메서드`()

                Then("1초 후 1을 반환한다.") {
                    result shouldBeEqual 1
                }
            }

            When("1을 반환하는 메서드를 호출하면") {
                val result = target.`1을 반환하는 메서드`()

                Then("바로 1을 반환한다.") {
                    result shouldBeEqual 1
                }
            }
        }
    }
}
