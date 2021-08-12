package com.trendyol.pipeline

class TestWriterStep(
    private val text: String,
) : PipelineStep<TestDataContext> {

    override suspend fun execute(context: TestDataContext, next: PipelineStepDelegate<TestDataContext>) {
        context.text = text
        next(context)
    }
}

