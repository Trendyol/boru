package com.trendyol.kafkaconsumerexample.pipeline

import com.trendyol.boru.PipelineStep

/**
 * A pipeline step that dispatches message to consumers by type
 */
class MessageDispatchStep : PipelineStep<KafkaMessageContext> {
    override suspend fun execute(context: KafkaMessageContext, next: suspend (KafkaMessageContext) -> Unit) {
        when (val message = context.message) {
            is MyKafkaEvent -> {
            } // dispatch your message to consumer
            is AnotherKafkaEvent -> {
            } // dispatch your message to consumer
            else -> throw IllegalStateException("kafka.messages.type.not.found")
        }
    }
}

class MyKafkaEvent
class AnotherKafkaEvent