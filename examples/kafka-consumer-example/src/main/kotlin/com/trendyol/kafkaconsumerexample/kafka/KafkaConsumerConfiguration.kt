package com.trendyol.kafkaconsumerexample.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import java.time.Duration

data class KafkaConsumerConfiguration(
    val topics: List<String>,
    val errorTopicName: String,
    val pollTimeout: Duration,
) {
    lateinit var onMessageReceivedHandler: OnMessageReceivedHandler
    fun onMessageReceived(handler: OnMessageReceivedHandler) {
        onMessageReceivedHandler = handler
    }
}

typealias OnMessageReceivedHandler = suspend (ConsumerRecord<String, String>) -> Unit