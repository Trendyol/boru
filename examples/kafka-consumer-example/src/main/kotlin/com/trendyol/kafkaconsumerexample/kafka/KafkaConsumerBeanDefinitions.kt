package com.trendyol.kafkaconsumerexample.kafka

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.trendyol.boru.Pipeline
import com.trendyol.boru.pipelineBuilder
import com.trendyol.boru.usePipelineStep
import com.trendyol.kafkaconsumerexample.pipeline.MessageDispatchStep
import com.trendyol.kafkaconsumerexample.pipeline.HeaderEnrichmentStep
import com.trendyol.kafkaconsumerexample.pipeline.KafkaMessageContext
import com.trendyol.kafkaconsumerexample.pipeline.MessageSerializerStep
import com.trendyol.kafkaconsumerexample.pipeline.SendFailedMessageToAnotherTopicStep
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import java.net.InetAddress
import java.time.Duration
import java.util.*

class KafkaConsumerBeanDefinitions {

    @Bean
    fun kafkaConsumer(): KafkaConsumer<String, String> {
        val configProperties = Properties().also {
            it[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost"
            it[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
            it[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = "org.apache.kafka.common.serialization.StringDeserializer"
            it[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
            it[ConsumerConfig.GROUP_ID_CONFIG] = "com.trendyol.example-consumer"
            it[ConsumerConfig.CLIENT_ID_CONFIG] = InetAddress.getLocalHost().hostName
        }

        return KafkaConsumer<String, String>(configProperties)
    }

    @Bean
    fun kafkaProducer(): KafkaProducer<String, String> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost"
        props[ProducerConfig.CLIENT_ID_CONFIG] = "com.trendyol.example-consumer"
        props[ProducerConfig.ACKS_CONFIG] = "1"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java

        return KafkaProducer<String, String>(props)
    }

    @Bean
    suspend fun kafkaConsumerPipeline(kafkaProducer: KafkaProducer<String, String>): Pipeline<KafkaMessageContext> {
        val objectMapper = ObjectMapper(JsonFactory())

        return pipelineBuilder {
            usePipelineStep(SendFailedMessageToAnotherTopicStep(kafkaProducer))
            usePipelineStep(HeaderEnrichmentStep())
            usePipelineStep(MessageSerializerStep(objectMapper))
            usePipelineStep(MessageDispatchStep())
        }
    }


    fun startKafkaConsumers(kafkaConsumerPipeline: Pipeline<KafkaMessageContext>, kafkaConsumer: KafkaConsumer<String, String>) {
        val userCreatedConsumerConfig = KafkaConsumerConfiguration(listOf("user.created"), "user.created.error", Duration.ofSeconds(30))
        val userDeletedConsumerConfig = KafkaConsumerConfiguration(listOf("user.deleted"), "user.deleted.error", Duration.ofSeconds(30))
        val userUpdatedConsumerConfig = KafkaConsumerConfiguration(listOf("user.updated"), "user.updated.error", Duration.ofSeconds(30))
        val consumerConfigs = listOf(
            userCreatedConsumerConfig,
            userDeletedConsumerConfig,
            userUpdatedConsumerConfig,
        )

        consumerConfigs.forEach { config ->
            config.onMessageReceivedHandler = {
                val context = KafkaMessageContext(it, config.errorTopicName)
                kafkaConsumerPipeline.execute(context)
            }

            val consumer = Consumer(kafkaConsumer, config)
            consumer.start()
        }
    }
}