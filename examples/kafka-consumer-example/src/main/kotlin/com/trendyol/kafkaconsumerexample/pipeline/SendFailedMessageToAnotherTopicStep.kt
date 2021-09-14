package com.trendyol.kafkaconsumerexample.pipeline

import com.trendyol.boru.PipelineStep
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

/**
 * A pipeline step that sends messages to error topic after receiving exceptions
 */
class SendFailedMessageToAnotherTopicStep(
    private val kafkaProducer: KafkaProducer<String, String>,
) : PipelineStep<KafkaMessageContext> {

    override suspend fun execute(context: KafkaMessageContext, next: suspend (KafkaMessageContext) -> Unit) {
        runCatching {
            next(context)
        }.onFailure { consumeError ->
            runCatching {
                val newHeaders = context.incomingMessage.headers().add("Error", consumeError.stackTraceToString().toByteArray())
                val record = ProducerRecord<String, String>(context.errorTopicName, null, null, context.incomingMessage.value(), newHeaders)
                kafkaProducer.send(record)
            }.onFailure { publishError ->
                // ignore
            }
        }
    }
}