package com.trendyol.boru

typealias PipelineStepDelegate<TContext> = suspend (TContext) -> Unit

typealias Func<T, TResult> = (T) -> TResult