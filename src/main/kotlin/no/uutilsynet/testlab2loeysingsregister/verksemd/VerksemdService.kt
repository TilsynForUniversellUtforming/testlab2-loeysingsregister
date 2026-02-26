package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.util.NoSuchElementException
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

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
          return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer", it))
        })
  }

  fun getBrregData(orgnummer: String): Result<BrregVerksemd> {
    val url = "${properties.url}/$orgnummer"

    val result = runCatching {
      RestClient.create(url).get().retrieve().body(BrregVerksemd::class.java)
          ?: throw NoSuchElementException("Fant ikkje verksemd med orgnummer $orgnummer")
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
