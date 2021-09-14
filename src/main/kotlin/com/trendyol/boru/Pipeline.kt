package com.trendyol.boru

class Pipeline<TContext>(
    private val pipelineStepDelegate: PipelineStepDelegate<TContext>,
) {
    suspend fun execute(context: TContext) {
        pipelineStepDelegate(context)
    }
}

