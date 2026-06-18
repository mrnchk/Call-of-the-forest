package com.cotf.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CotfServerApplication

fun main(args: Array<String>) {
    runApplication<CotfServerApplication>(*args)
}
