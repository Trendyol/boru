package com.trendyol.pipeline

interface PipelineStep<TContext : PipelineContext> {
    suspend fun execute(context: TContext, next: PipelineStepDelegate<TContext>)
}