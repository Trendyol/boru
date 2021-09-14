package com.trendyol.kafkaconsumerexample.pipeline

import com.fasterxml.jackson.databind.ObjectMapper
import com.trendyol.boru.PipelineStep

/**
 * A pipeline step that deserializes messages using SerializerKey in message headers
 */
class MessageSerializerStep(
    private val objectMapper: ObjectMapper,
) : PipelineStep<KafkaMessageContext> {
    override suspend fun execute(context: KafkaMessageContext, next: suspend (KafkaMessageContext) -> Unit) {
        val type = context.incomingMessage.headers().firstOrNull { it.key() == "SerializerKey" } ?: throw Exception("serializerKey.not.found")
        val clazz = Class.forName(String(type.value())).javaClass
        val message = objectMapper.readValue(context.incomingMessage.value(), clazz)
        context.message = message
        next(context)
    }
}