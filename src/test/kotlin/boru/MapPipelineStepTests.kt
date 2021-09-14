package com.trendyol.boru

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class MapPipelineStepTests {
    @Test
    fun `should not be deadlocked when there is not alternative branch`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .map({ it.intValue == 1 }) {
                    usePipelineStep(TestWriterStep("one"))
                }.build()

            val firstContext = TestDataContext().apply {
                this.intValue = 1
            }

            val secondContext = TestDataContext().apply {
                this.intValue = 2
            }

            //when
            pipeline.execute(firstContext)
            pipeline.execute(secondContext)

            //then
            assertEquals(firstContext.intValue, 1)
            assertEquals(firstContext.text, "one")

            assertEquals(secondContext.intValue, 2)
            assertEquals(secondContext.text, "null")
        }
    }

    fun test() {
        runBlocking {
            val a = pipelineBuilder<TestDataContext> {
                map({ false }) {

                }
            }
        }
    }

    @Test
    fun `should execute different pipelines in different branches`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .map({ context -> context.intValue == 1 }) {
                    usePipelineStep(TestWriterStep("one"))
                }.use { context: TestDataContext, next: suspend () -> Unit ->
                    context.items["Finished"] = true
                    next()
                }.build()

            val firstContext = TestDataContext().apply {
                this.intValue = 1
            }
            val secondContext = TestDataContext().apply {
                this.intValue = 2
            }

            //when
            pipeline.execute(firstContext)
            pipeline.execute(secondContext)

            //then 
            assertEquals(firstContext.intValue, 1)
            assertEquals(firstContext.text, "one")
            assertFalse(firstContext.items.contains("Finished"))

            assertEquals(secondContext.intValue, 2)
            assertEquals(secondContext.text, "null")
            assertTrue(secondContext.items.contains("Finished"))
        }
    }

    @Test
    fun `should execute different child pipelines by predicate`() {
        runBlocking {
            //given
            val pipeline = PipelineBuilder<TestDataContext>()
                .use { context: TestDataContext, next: suspend () -> Unit ->
                    context.items["user"] = "bilal.kilic"
                    next()
                }
                .map({ context -> context.intValue == 1 }) {
                    usePipelineStep(TestWriterStep("one"))
                }
                .map({ context -> context.intValue == 2 }) {
                    usePipelineStep(TestWriterStep("two"))
                }.build()

            val firstContext = TestDataContext().apply {
                this.intValue = 1
            }
            val secondContext = TestDataContext().apply {
                this.intValue = 2
            }

            //when
            pipeline.execute(firstContext)
            pipeline.execute(secondContext)

            //then
            assertEquals(firstContext.intValue, 1)
            assertEquals(firstContext.text, "one")
            assertEquals(firstContext.items["user"], "bilal.kilic")

            assertEquals(secondContext.intValue, 2)
            assertEquals(secondContext.text, "two")
            assertEquals(secondContext.items["user"], "bilal.kilic")
        }
    }
}