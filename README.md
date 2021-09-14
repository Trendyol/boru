# PIPELINE

Pipeline implementation in kotlin with native coroutine support and custom dsl.

Supports chaining pipeline steps with conditions and branches

```
<dependency>
    <groupId>com.trendyol</groupId>
    <artifactId>pipeline</artifactId>
    <version>1.0.0</version>
</dependency>
```

## USAGE

#### Defining Context

Define a context implementing PipelineContext. In this case a simple context that sets and gets a text field

```kotlin
interface PipelineContext {
    val items: Map<Any, Any>
}

class TestDataContext : PipelineContext {
    override val items: MutableMap<Any, Any> = mutableMapOf()

    var intValue: Int = 0

    var text: String?
        get() = items.getOrDefault("Text", null).toString()
        set(value) {
            items["Text"] = value!!
        }
}
```

#### Define Pipeline Steps

Implement a pipeline step using TestDataContext

```kotlin
class TestWriterStep(
    private val text: String,
) : PipelineStep<TestDataContext> {

    override suspend fun execute(context: TestDataContext, next: PipelineStepDelegate<TestDataContext>) {
        context.text = text
        next(context)
    }
}
```

#### Build Pipeline

```kotlin
fun compose() {
    val pipeline = PipelineBuilder<TestDataContext>()
        .usePipelineStep(TestWriterStep("hello"))
        .build()
    val context = TestDataContext()
}
```

You can also use lambda functions without defining a pipeline step

```kotlin
fun compose() {
    val pipeline = PipelineBuilder<TestDataContext>()
        .use { context: TestDataContext, next: suspend () -> Unit ->
            context.text = "hello"
            next()
        }
        .build()
    val context = TestDataContext()
}
```

Or use built-in dsl

```kotlin
fun compose() {
    val pipeline = pipelineBuilder<TestDataContext> {
        use { testDataContext: TestDataContext, next: suspend () -> Unit ->
            context.text = "Hello World"
            next()
        }
    }
}
```

#### Conditions and Branching

You can also use conditions when executing steps or branch using map operation

```kotlin
fun compose() {
    val pipeline = pipelineBuilder<TestDataContext> {
        usePipelineStepWhen(TestWriterStep("Hello World")) {
            it.text == "ExecuteStep"
        }
    }
}
```

Mapping allows you to group multiple steps under one condition.

```kotlin
fun composeMapping() {
    val pipeline = pipelineBuilder<TestDataContext> {
        map({ it.intValue < 3 }) {
            usePipelineStep(TestWriterStep("one"))
            usePipelineStep(TestWriterStep("two"))
        }
        map({ it.intValue == 3 }) {
            usePipelineStep(TestWriterStep("three"))
        }
    }
}
```