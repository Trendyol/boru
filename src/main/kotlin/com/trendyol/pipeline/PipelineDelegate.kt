package com.trendyol.pipeline

typealias PipelineStepDelegate<TContext> = suspend (TContext) -> Unit

typealias Func<T, TResult> = (T) -> TResult