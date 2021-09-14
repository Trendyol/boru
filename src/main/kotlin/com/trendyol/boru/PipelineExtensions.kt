package com.trendyol.boru

suspend fun <TContext : PipelineContext> PipelineBuilder<TContext>.use(step: suspend (TContext, next: suspend () -> Unit) -> Unit): PipelineBuilder<TContext> {
    return this.use { next ->
        { context ->
            val simpleNextStep = suspend { next(context) }
            step(context, simpleNextStep)
        }
    }
}

@JvmName("useTContext")
suspend fun <TContext : PipelineContext> PipelineBuilder<TContext>.use(step: suspend (TContext, next: suspend (TContext) -> Unit) -> Unit): PipelineBuilder<TContext> {
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