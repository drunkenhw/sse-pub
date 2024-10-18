package org.example.ssepub2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class SsePub2Application

fun main(args: Array<String>) {
    runApplication<SsePub2Application>(*args)
}
