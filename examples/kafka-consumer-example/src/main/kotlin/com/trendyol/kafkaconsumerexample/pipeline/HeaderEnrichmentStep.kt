package com.trendyol.kafkaconsumerexample.pipeline

import com.trendyol.boru.PipelineStep
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext

/**
 * A pipeline step that put headers in kafka message to MDCContext
*/
class HeaderEnrichmentStep : PipelineStep<KafkaMessageContext> {
    override suspend fun execute(context: KafkaMessageContext, next: suspend (KafkaMessageContext) -> Unit) {
        val headers = context.incomingMessage
            .headers()
            .associate {
                it.key() to String(it.value())
            }

        withContext(MDCContext(headers)) {
            next(context)
        }
    }
}