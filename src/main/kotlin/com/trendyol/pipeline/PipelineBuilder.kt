package com.trendyol.pipeline

class PipelineBuilder<TContext : PipelineContext> {
    private val steps: MutableList<Func<PipelineStepDelegate<TContext>, PipelineStepDelegate<TContext>>> = mutableListOf()

    fun use(step: Func<PipelineStepDelegate<TContext>, PipelineStepDelegate<TContext>>): PipelineBuilder<TContext> {
        steps.add(step)
        return this
    }

    fun map(condition: Func<TContext, Boolean>, configuration: PipelineBuilder<TContext>.() -> Unit): PipelineBuilder<TContext> {
        val branchBuilder = this.new()
        configuration(branchBuilder)
        val branch = branchBuilder.build()

        val options = MapOptions(condition, branch)
        return this.use { next -> MapPipelineStep(next, options)::execute }
    }

    fun build(): Pipeline<TContext> {
        var step: PipelineStepDelegate<TContext> = { _ -> }

        for (i in steps.count() - 1 downTo 0) {
            step = steps[i](step)
        }

        return Pipeline(step)
    }

    fun new(): PipelineBuilder<TContext> {
        return PipelineBuilder()
    }
}

suspend fun <TContext : PipelineContext> pipelineBuilder(init: suspend PipelineBuilder<TContext>.() -> Unit): Pipeline<TContext> {
    val builder = PipelineBuilder<TContext>()
    init.invoke(builder)
    return builder.build()
}
