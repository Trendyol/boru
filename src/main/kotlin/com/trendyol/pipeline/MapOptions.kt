package com.trendyol.pipeline

data class MapOptions<TContext : PipelineContext>(
    val condition: Func<TContext, Boolean>,
    val branch: Pipeline<TContext>,
)