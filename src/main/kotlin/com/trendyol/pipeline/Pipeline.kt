package com.trendyol.pipeline

class Pipeline<TContext>(
    private val pipelineStepDelegate: PipelineStepDelegate<TContext>,
) {
    suspend fun execute(context: TContext) {
        pipelineStepDelegate(context)
    }
}

