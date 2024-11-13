package no.uutilsynet.testlab2loeysingsregister

import no.uutilsynet.testlab2securitylib.interceptor.ApiTokenInterceptor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@ConfigurationPropertiesScan
class Testlab2loeysingsregisterApplication {

  @Bean
  @Profile("!test")
  fun restTemplate(
      restTemplateBuilder: RestTemplateBuilder,
      apiTokenInterceptor: ApiTokenInterceptor
  ): RestTemplate {

    val interceptors: ArrayList<ClientHttpRequestInterceptor> = ArrayList()
    interceptors.add(apiTokenInterceptor)

    return restTemplateBuilder.interceptors(interceptors).build()
  }

  fun main(args: Array<String>) {
    runApplication<Testlab2loeysingsregisterApplication>(*args)
  }
}

@RestController
class AppNameResource {
  @GetMapping("/") fun appName() = mapOf("appName" to "testlab2-loeysingsregister")
}
