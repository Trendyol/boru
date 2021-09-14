package com.trendyol.boru

data class MapOptions<TContext : PipelineContext>(
    val condition: Func<TContext, Boolean>,
    val branch: Pipeline<TContext>,
)