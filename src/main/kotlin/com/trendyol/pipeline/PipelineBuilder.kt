package com.trendyol.pipeline

class PipelineBuilder<TContext : PipelineContext> {
    private val steps: MutableList<Func<PipelineStepDelegate<TContext>, PipelineStepDelegate<TContext>>> = mutableListOf()

    fun use(step: Func<PipelineStepDelegate<TContext>, PipelineStepDelegate<TContext>>): PipelineBuilder<TContext> {
        steps.add(step)
        return this
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