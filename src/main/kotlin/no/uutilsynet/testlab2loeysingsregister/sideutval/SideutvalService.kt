package no.uutilsynet.testlab2loeysingsregister.sideutval

import java.net.URI
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingEntity
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingRepository
import org.springframework.stereotype.Service

@Service
class SideutvalService(
    private val sideutvalRepository: SideutvalRepository,
    private val loeysingRepository: LoeysingRepository
) {

  fun getSideutvalFromSide(side: String): Result<Sideutval> {
    return runCatching {
      sideutvalRepository.findSideutvalBySide(side)
          ?: throw NoSuchElementException("Fant ingen sideutval for side: $side")
    }
  }

  fun getOrCreateSideutval(
      sideutvalLoolkupRequest: SideutvalLookupRequest
  ): Result<List<SideUtvalResponse>> {

    return getLoeysingFromNameUrl(sideutvalLoolkupRequest).mapCatching { loeysing ->
      sideutvalLoolkupRequest.address.map { side ->
        requireNotNull(loeysing.id) { "LoeysingId manglar" }
        getSideBySideAndLoeysingId(side, loeysing.id!!)
            .recover { createSideutval(side, loeysing.id!!) }
            .getOrThrow()
            .toSideutvalResponse()
      }
    }
  }

  private fun Sideutval.toSideutvalResponse(): SideUtvalResponse {
    return SideUtvalResponse(this.id, this.side)
  }

  fun getLoeysingFromNameUrl(
      sideutvalLookupRequest: SideutvalLookupRequest
  ): Result<LoeysingEntity> = runCatching {
    loeysingRepository.findByUrl(URI(sideutvalLookupRequest.loeysingNamn).toURL()).firstOrNull()
        ?: throw NoSuchElementException(
            "Fant ingen loeysing for url: ${sideutvalLookupRequest.loeysingNamn}")
  }

  fun getSideBySideAndLoeysingId(address: String, loeysingId: Int): Result<Sideutval> {
    return runCatching {
      sideutvalRepository.findBySideAndLoeysingId(address, loeysingId)
          ?: throw NoSuchElementException(
              "Fant ingen sideutval for side: $address og loeysingId: $loeysingId")
    }
  }

  fun createSideutval(side: String, loeysingId: Int): Sideutval {
    val loeysing =
        loeysingRepository.findById(loeysingId.toLong()).orElseThrow {
          NoSuchElementException("Fant ingen loeysing for id: $loeysingId")
        }

    val sideutval =
        Sideutval(
            type = null, loeysing = loeysing, sidetype = Sidetype.NETTSIDE, side = side, id = 0)



    return sideutvalRepository.save(sideutval)
  }

  fun getSideutval(request: SideutvalLookupRequest): SideutvalLookupResponse {
    val loysing = getLoeysingFromNameUrl(request).getOrThrow()
    requireNotNull(loysing.id)
    val sideutvalList = getOrCreateSideutval(request).getOrThrow()
    return SideutvalLookupResponse(loysing.id!!, sideutvalList)
  }
}
