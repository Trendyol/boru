package com.trendyol.kafkaconsumerexample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaConsumerExampleApplication

fun main(args: Array<String>) {
    runApplication<KafkaConsumerExampleApplication>(*args)
}
