package com.trendyol.boru

class MapPipelineStep<TContext : PipelineContext>(
    private val next: PipelineStepDelegate<TContext>,
    private val options: MapOptions<TContext>,
) {
    suspend fun execute(context: TContext) {
        if (options.condition(context)) {
            options.branch.execute(context)
        } else {
            next(context)
        }
    }
}