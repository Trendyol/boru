package com.trendyol.pipeline

suspend fun <TContext : PipelineContext> PipelineBuilder<TContext>.use(step: suspend (TContext, suspend () -> Unit) -> Unit): PipelineBuilder<TContext> {
    return this.use { next ->
        { context ->
            val simpleNextStep = suspend { next(context) }
            step(context, simpleNextStep)
        }
    }
}

@JvmName("useTContext")
suspend fun <TContext : PipelineContext> PipelineBuilder<TContext>.use(step: suspend (TContext, suspend (TContext) -> Unit) -> Unit): PipelineBuilder<TContext> {
    return this.use { next ->
        { context ->
            val simpleNextStep: suspend (TContext) -> Unit = { c: TContext -> next(c) }
            step(context, simpleNextStep)
        }
    }
}

fun <TContext : PipelineContext> PipelineBuilder<TContext>.usePipelineStepWhen(step: PipelineStep<TContext>, condition: Func<TContext, Boolean>): PipelineBuilder<TContext> {
    return this.use { next ->
        { context ->
            if (condition(context)) {
                step.execute(context, next)
            } else {
                next(context)
            }
        }
    }
}

fun <TContext : PipelineContext> PipelineBuilder<TContext>.usePipelineStep(step: PipelineStep<TContext>): PipelineBuilder<TContext> {
    return this.use { next ->
        { context ->
            step.execute(context, next)
        }
    }
}

fun <TContext : PipelineContext> PipelineBuilder<TContext>.map(condition: Func<TContext, Boolean>, configuration: (PipelineBuilder<TContext>) -> Unit): PipelineBuilder<TContext> {
    val branchBuilder = this.new()
    configuration(branchBuilder)
    val branch = branchBuilder.build()

    val options = MapOptions(condition, branch)
    return this.use { next -> MapPipelineStep(next, options)::execute }
}

data class MapOptions<TContext : PipelineContext>(
    val condition: Func<TContext, Boolean>,
    val branch: Pipeline<TContext>,
)

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
