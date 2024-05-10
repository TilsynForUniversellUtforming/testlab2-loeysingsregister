package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class VerksemdService(val properties: BrregRegisterProperties) {

  val logger = LoggerFactory.getLogger(VerksemdService::class.java)

  fun getVerksemdData(orgnummer: String): Result<NyVerksemd> {

    val result = getBrregData(orgnummer)

    result.fold(
        onSuccess = { brregVerksemd ->
          return Result.success(NyVerksemd(brregVerksemd))
        },
        onFailure = {
          return Result.success(createVerksemdUtanforBrreg(orgnummer))
        })
  }

  fun getBrregData(orgnummer: String): Result<BrregVerksemd> {
    val url = "${properties.url}/$orgnummer"

    val result = runCatching {
      WebClient.create().get().uri(url).retrieve().bodyToMono(BrregVerksemd::class.java).block()
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

  fun createVerksemdUtanforBrreg(orgnummer: String): NyVerksemd {
    return NyVerksemd(
        namn = "Virksomhet ikkje i brreg",
        organisasjonsnummer = orgnummer,
    )
  }
}

@ConfigurationProperties(prefix = "brreg") data class BrregRegisterProperties(val url: String)
