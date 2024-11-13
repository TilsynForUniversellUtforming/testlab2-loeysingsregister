package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class VerksemdService(
    val properties: BrregRegisterProperties,
    val restTemplateBuilder: RestTemplateBuilder
) {

  val logger = LoggerFactory.getLogger(VerksemdService::class.java)

  fun getVerksemdData(orgnummer: String): Result<NyVerksemd> {

    val result = getBrregData(orgnummer)

    result.fold(
        onSuccess = { brregVerksemd ->
          return Result.success(NyVerksemd(brregVerksemd))
        },
        onFailure = {
          return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer", it))
        })
  }

  fun getBrregData(orgnummer: String): Result<BrregVerksemd> {
    val url = "${properties.url}/$orgnummer"
    val restTemplate = restTemplateBuilder.build()
    val restClient = RestClient.builder(restTemplate).build()

    val result = runCatching {
      restClient.get().uri(url).retrieve().body(BrregVerksemd::class.java)
          ?: throw Exception("Fant ikkje verksemd med orgnummer $orgnummer")
    }

    result.fold(
        onSuccess = { brregVerksemd ->
          return Result.success(brregVerksemd)
        },
        onFailure = {
          return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer", it))
        })
  }
}

@ConfigurationProperties(prefix = "brreg") data class BrregRegisterProperties(val url: String)
