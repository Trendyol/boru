package com.trendyol.kafkaconsumerexample.kafka

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * A consumer job that pulls messages from kafka and processes them with handler provided in [KafkaConsumerConfiguration]
 */
class Consumer(
    private val consumer: KafkaConsumer<String, String>,
    private val config: KafkaConsumerConfiguration,
) : CoroutineScope {
    val logger: Logger = LoggerFactory.getLogger(Consumer::class.java)

    private val job: Job = SupervisorJob()
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    override val coroutineContext: CoroutineContext
        get() = job + Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun start() {
        consumer.subscribe(config.topics, object : ConsumerRebalanceListener {
            override fun onPartitionsRevoked(partitions: Collection<TopicPartition>) {
                logger.info("${partitions.joinToString()} topic-partitions are revoked from this consumer")
            }

            override fun onPartitionsAssigned(partitions: Collection<TopicPartition?>) {
                logger.info("${partitions.joinToString()} topic-partitions are assigned to this consumer")
            }
        })

        job.invokeOnCompletion {
            logger.error("Consumer is stopping", it)
        }

        startConsumer()
    }

    private fun startConsumer() = launch {
        repeatUntilCancelled {
            val records = consumer.poll(config.pollTimeout)

            if (records.isEmpty) {
                delay(1000)
                return@repeatUntilCancelled
            }

            records.forEach {
                config.onMessageReceivedHandler(it)
            }

            consumer.commitAsync { mutableMap, exception ->
                if (exception != null) {
                    logger.debug("Commit failed for offsets, $mutableMap", exception)
                }
            }
        }
    }

    private suspend fun repeatUntilCancelled(block: suspend () -> Unit) {
        while (isActive) {
            try {
                block()
                yield()
            } catch (ex: CancellationException) {
                logger.error("Kafka consumer cancelled", ex)
                stop()
            } catch (ex: Exception) {
                logger.error("Error occurred while consuming messages", ex)
            }
        }
    }

    private fun stop() {
        consumer.close()
        job.cancel()
        singleThreadExecutor.shutdown()
    }
}