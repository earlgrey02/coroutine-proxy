## Coroutine Proxy

> **코루틴(Coroutine)의 일시 중지 함수에 대한 동적 프록시(Dynamic Proxy) 지원**

본 저장소는 코루틴의 내부 원리를 통해 일시 중지 함수에 동적 프록시를 적용한 예제에 대한 저장소이다.

## Description

### Dynamic Proxy

동적 프록시란 코드의 수정 없이 부가 기능을 추가하기 위해 사용하는 프록시를 런타임에 동적으로 구현해 적용하는 것을 의미한다.
이러한 동적 프록시를 사용하는 대표적인 예시로 AOP(Aspect Oriented Programming)가 있다.

JVM 진영에서 동적 프록시는 일반적으로 JDK나 CGLib(Code Generator Library) 등을 통해 구현한다.

### Dynamic Proxy with Coroutine

기본적으로 JDK나 CGLib(Code Generator Library)는 코루틴의 일시 중지 함수에 대한 프록시를 지원하지 않는다.

```java
public interface InvocationHandler {
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable;
}
```

실제로 JDK의 `InvocationHandler`에서는 `invoke()`가 일시 중지 함수가 아니므로 내부에서 코루틴을 사용할 수 없다.<br/>
물론 `runBlocking()`이나 `GlobalScope`를 사용하면 일반 함수에서도 코루틴을 사용할 수 있지만, 이는 논블로킹(Non-blocking)이나 컨텍스트 관점에서 적절한 해결 방법이 아니다.

하지만 CPS(Continuation Passing Style)를 기반으로 작동하는 코루틴의 특징을 활용하면 `suspend` 키워드 없이 일시 중지 함수를 구현할 수 있는데,
이를 활용해 동적 프록시에도 코루틴을 적용할 수 있다.

```kotlin
class CoroutineTest : AnnotationSpec() {
    @Test
    suspend fun test() {
        delay(1000)
        delay(1000)
    }
}
```

```java
public final class CoroutineTest extends AnnotationSpec {
    @Test
    @Nullable
    public final Object test(@NotNull Continuation $completion) {
        Object $continuation;
        label27:
        {
            if ($completion instanceof <undefinedtype >){
            $continuation = ( < undefinedtype >)$completion;
            if (((( < undefinedtype >) $continuation).label & Integer.MIN_VALUE) !=0){
                (( < undefinedtype >) $continuation).label -= Integer.MIN_VALUE;
                break label27;
            }
        }

            $continuation = new ContinuationImpl($completion) {
                // $FF: synthetic field
                Object result;
                int label;

                @Nullable
                public final Object invokeSuspend(@NotNull Object $result) {
                    this.result = $result;
                    this.label |= Integer.MIN_VALUE;
                    return CoroutineTest.this.test((Continuation) this);
                }
            };
        }

        Object $result = (( < undefinedtype >) $continuation).result;
        Object var4 = IntrinsicsKt.getCOROUTINE_SUSPENDED();
        switch ((( < undefinedtype >) $continuation).label){
            case 0:
                ResultKt.throwOnFailure($result);
                (( < undefinedtype >) $continuation).label = 1;
                if (DelayKt.delay(1000L, (Continuation) $continuation) == var4) {
                    return var4;
                }
                break;
            case 1:
                ResultKt.throwOnFailure($result);
                break;
            case 2:
                ResultKt.throwOnFailure($result);
                return Unit.INSTANCE;
            default:
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }

        (( < undefinedtype >) $continuation).label = 2;
        if (DelayKt.delay(1000L, (Continuation) $continuation) == var4) {
            return var4;
        } else {
            return Unit.INSTANCE;
        }
    }
}

```

Kotlin의 코루틴은 State Machine으로 `Continuation`을 사용하는데, 이 `Continuation`은 컴파일 후 바이트 코드에서 일시 중지 함수의 마지막 인자로 생성된다.<br/>
또한 일시 중지 함수는 내부적으로 `COROUTINE_SUSPENDED`가 반환되면 중지되며, 이후 `Continuation`의 `resumeWith()`가 호출되면 `Continuation`에 저장된
상태를 바탕으로 재개되는 방식으로 작동한다.<br/>
그래서 일시 중지 함수의 바이트 코드를 보면 `COROUTINE_SUSPENDED`를 반환하기 위해 반환 타입이 `Object`로 정의되어 있다.

```kotlin
@SinceKotlin("1.3")
public interface Continuation<in T> {
    public val context: CoroutineContext

    public fun resumeWith(result: Result<T>)
}
```

```kotlin
@SinceKotlin("1.3")
public val COROUTINE_SUSPENDED: Any get() = CoroutineSingletons.COROUTINE_SUSPENDE
```

이러한 내부적인 원리를 컴파일러가 아닌 개발자가 `Continuation`과 `COROUTINE_SUSPENDED`를 직접 사용해 구현하면 일반 함수로도 일시 중지 함수와 같은 기능을 수행할 수 있다.

```kotlin
internal fun <T> Continuation<T>.coroutineScope(block: suspend () -> T): Any =
    with(CoroutineScope(context)) {
        launch {
            runCatching { block() }
                .run(::resumeWith)
        }

        COROUTINE_SUSPENDED
    }
```

우선 `Continuation` 내의 코루틴 컨텍스트를 상속 받는 코루틴 스코프에서 일시 중지 함수를 호출하는 `coroutineScope()`를 구현했다.<br/>
해당 확장 함수는 `suspend` 키워드가 없으므로 일반 함수에서도 호출이 가능하다.

```kotlin
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
```

그 다음, 프록시의 메서드 호출 시 실제로 호출되는 `InvocationHandler`의 코루틴 변형인 `CoInvocationHandler`을 구현했다.
`CoInvocationHandelr`에서는 프록시의 메서드가 받은 마지막 인자인 `Continuation`을 가져오고, 해당 `Continuation`의 `coroutineScope()`를 통해 실제로 호출할
일시 중지 함수를 사용하도록 한다.

이렇게 하면 `suspend` 키워드가 붙은 일시 중지 함수에 대해서도 런타임에 프록시를 적용할 수 있게 된다.

## How to use

본 예제는 JDK의 `InvocationHandler`와 CGLib의 `MethodInterceptor`에 대한 코루틴 변형만 구현되어 있다.<br/>
실제 동작은 테스트에서 확인할 수 있다.
