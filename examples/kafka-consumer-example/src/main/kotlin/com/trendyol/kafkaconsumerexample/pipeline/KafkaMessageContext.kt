package com.trendyol.kafkaconsumerexample.pipeline

import com.trendyol.boru.PipelineContext
import org.apache.kafka.clients.consumer.ConsumerRecord

class KafkaMessageContext(
    val incomingMessage: ConsumerRecord<String, String>,
    val errorTopicName: String
) : PipelineContext {
    lateinit var message: Any
    override val items: Map<Any, Any>
        get() = mutableMapOf()
}