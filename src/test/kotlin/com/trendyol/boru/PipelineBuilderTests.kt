package com.trendyol.boru

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PipelineBuilderTests {

    @Test
    fun `should execute pipeline step object`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .usePipelineStep(TestWriterStep("hello"))
                .build()
            val context = TestDataContext()

            //when
            pipeline.execute(context)

            //then
            assertEquals(context.text, "hello")
        }
    }

    @Test
    fun `should execute pipeline step with empty next`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .use { context: TestDataContext, next: suspend () -> Unit ->
                    context.text = "hello"
                    next()
                }
                .build()
            val context = TestDataContext()

            //when
            pipeline.execute(context)

            //then
            assertEquals(context.text, "hello")
        }
    }

    @Test
    fun `should execute pipeline step with next context`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .use { context: TestDataContext, next: suspend (TestDataContext) -> Unit ->
                    context.text = "hello"
                    next(context)
                }
                .build()
            val context = TestDataContext()

            //when
            pipeline.execute(context)

            //then
            assertEquals(context.text, "hello")
        }
    }

    @Test
    fun `should execute pipeline step delegate`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .use { next ->
                    { context ->
                        context.text = "1"
                        next(context)
                    }
                }
                .build()

            val context = TestDataContext()

            //when
            pipeline.execute(context)

            //then
            assertEquals(context.text, "1")
        }
    }

    @Test
    fun `should execute inner pipeline steps`() {
        runBlocking {
            //given
            val pipeLine = PipelineBuilder<TestDataContext>()
                .use { next ->
                    { context ->
                        context.text = "hello"
                        next(context)
                    }
                }.use { next ->
                    { context ->
                        context.text += " world"
                        next(context)
                    }
                }.build()

            val context = TestDataContext()

            //when
            pipeLine.execute(context)

            //then
            assertEquals(context.text, "hello world")
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["execute conditional step", "dont execute step"])
    fun `should execute step on condition`(input: String) {
        runBlocking {
            //given
            val conditionalStep = TestWriterStep("changed new input")
            val pipeLine = PipelineBuilder<TestDataContext>()
                .usePipelineStepWhen(conditionalStep) { context ->
                    context.text == "execute conditional step"
                }
                .build()

            val context = TestDataContext()
            context.text = input // set initial text to input

            //when
            pipeLine.execute(context)

            //then
            when (input) {
                "execute conditional step" -> assertEquals(context.text, "changed new input")
                "dont execute step" -> assertEquals(context.text, "dont execute step")
            }
        }
    }
}