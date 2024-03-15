package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class VerksemdService() {

    val brregApi = "https://data.brreg.no/enhetsregisteret/api/enheter"
    val logger = LoggerFactory.getLogger(VerksemdService::class.java)


    fun getVerksemdData(orgnummer: String): Result<NyVerksemd> {

        val result = getBrregData(orgnummer)

        result.fold(
            onSuccess = { brregVerksemd ->
                return Result.success(
                    NyVerksemd(brregVerksemd)
                )
            },
            onFailure = {
                return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer"))
            })
    }

    fun getBrregData(orgnummer: String): Result<BrregVerksemd> {
        val result = runCatching {
            WebClient.create()
                .get()
                .uri("$brregApi/$orgnummer")
                .retrieve()
                .bodyToMono(BrregVerksemd::class.java)
                .block() ?: throw Exception("Fant ikkje verksemd med orgnummer $orgnummer")
        }

        result.fold(
            onSuccess = { brregVerksemd ->
                return Result.success(brregVerksemd)
            },
            onFailure = {
                return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer"))
            })
    }


}
