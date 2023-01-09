package no.uutilsynet.testlab2loeysingsregister

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication class Testlab2loeysingsregisterApplication

fun main(args: Array<String>) {
  runApplication<Testlab2loeysingsregisterApplication>(*args)
}

@RestController
class AppNameResource {
  @GetMapping("/") fun appName() = mapOf("appName" to "testlab2-loeysingsregister")
}
