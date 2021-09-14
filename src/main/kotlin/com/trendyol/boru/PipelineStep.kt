package com.trendyol.boru

interface PipelineStep<TContext : PipelineContext> {
    suspend fun execute(context: TContext, next: PipelineStepDelegate<TContext>)
}