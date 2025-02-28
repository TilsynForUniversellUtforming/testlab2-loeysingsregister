package no.uutilsynet.testlab2loeysingsregister.loeysing

import java.time.Instant
import no.uutilsynet.testlab2loeysingsregister.*
import no.uutilsynet.testlab2loeysingsregister.verksemd.Verksemd
import no.uutilsynet.testlab2loeysingsregister.verksemd.VerksemdDAO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

private const val FEILA_DAA_VI_SKULLE_HENTE_LOEYSINGAR = "Feila då vi skulle hente løysingar"

@RestController
@RequestMapping("v1/loeysing")
class LoeysingResource(val loeysingDAO: LoeysingDAO, val verksemdDAO: VerksemdDAO) {
  val logger: Logger = LoggerFactory.getLogger(LoeysingResource::class.java)

  @PostMapping
  fun createLoeysing(@RequestBody body: Map<String, String>) =
      runCatching {
            val namn = validateNamn(body["namn"]).getOrThrow()
            val url = validateURL(body["url"]).getOrThrow()
            val orgnummer = validateOrgNummer(body["orgnummer"]).getOrThrow()
            val verksemd = verksemdDAO.getVerksemdByOrgnummer(orgnummer).getOrNull()
            val verksemdId = verksemd?.id

            loeysingDAO.createLoeysing(namn, url, orgnummer, verksemdId).also {
              logger.info("lagra løysing ($url, $orgnummer)")
            }
          }
          .fold(
              { id ->
                val location =
                    ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(id)
                        .toUri()
                ResponseEntity.created(location).build()
              },
              { exception ->
                when (exception) {
                  is NullPointerException -> ResponseEntity.badRequest().body(exception.message)
                  is IllegalArgumentException -> ResponseEntity.badRequest().body(exception.message)
                  else -> ResponseEntity.internalServerError().body(exception.message)
                }
              })

  @GetMapping("{id}")
  fun getLoeysing(@PathVariable id: Int, @RequestParam atTime: String?): ResponseEntity<Loeysing> {
    val instant = atTime?.let { validateInstant(atTime).getOrThrow() } ?: Instant.now()
    return loeysingDAO.getLoeysing(id, instant)?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.notFound().build()
  }

  @GetMapping
  fun getMany(
      @RequestParam ids: String?,
      @RequestParam search: String?,
      @RequestParam atTime: String?
  ): ResponseEntity<List<Loeysing>> {
    return getManyBase(atTime, ids, search)
        .fold(
            { ResponseEntity.ok(it) },
            { exception ->
              logger.error(FEILA_DAA_VI_SKULLE_HENTE_LOEYSINGAR, exception)
              when (exception) {
                is IllegalArgumentException -> ResponseEntity.badRequest().build()
                else -> ResponseEntity.internalServerError().build()
              }
            })
  }

  @GetMapping("verksemd")
  fun getManyByVerksemd(
      @RequestParam search: String?,
      @RequestParam atTime: String?
  ): ResponseEntity<List<Loeysing>> {
    return runCatching { getManyByVerksemdBase(search, atTime) }
        .fold(
            { ResponseEntity.ok(it) },
            { exception ->
              logger.error(FEILA_DAA_VI_SKULLE_HENTE_LOEYSINGAR, exception)
              when (exception) {
                is IllegalArgumentException -> ResponseEntity.badRequest().build()
                else -> ResponseEntity.internalServerError().build()
              }
            })
  }

  private fun getManyByVerksemdBase(search: String?, atTime: String?): List<Loeysing> {
    val instant = atTime?.let { validateInstant(atTime).getOrThrow() } ?: Instant.now()
    return when {
      search?.isNotBlank() == true -> {
        loeysingDAO.findLoeysingarByVerksemd(search, instant)
      }
      else -> {
        loeysingDAO.getLoeysingList(atTime = instant)
      }
    }
  }

  private fun getManyBase(atTime: String?, ids: String?, search: String?) = runCatching {
    val instant = getValidatedTimeOrInstantNow(atTime)
    val validatedIds = ids?.let { validateIdList(ids).getOrThrow() }
    if (search?.isNotBlank() == true) {
      loeysingDAO.findLoeysingar(search, instant).filter { filterLoeysingar(validatedIds, it.id) }
    } else {
      loeysingDAO.getLoeysingList(validatedIds, atTime = instant)
    }
  }

  private fun filterLoeysingar(ids: List<Int>?, id: Int): Boolean {
    return ids?.contains(id) ?: true
  }

  private fun getValidatedTimeOrInstantNow(atTime: String?): Instant =
      atTime?.let { validateInstant(atTime).getOrThrow() } ?: Instant.now()

  @PutMapping
  fun update(@RequestBody loeysing: Loeysing): ResponseEntity<Unit> =
      runCatching {
            val namn = validateNamn(loeysing.namn).getOrThrow()
            val orgnummer = validateOrgNummer(loeysing.orgnummer).getOrThrow()
            var verksemdId = loeysing.verksemdId

            if (verksemdId == null) {
              verksemdId = verksemdDAO.getVerksemdByOrgnummer(orgnummer).getOrNull()?.id
            }
            val validated = Loeysing(loeysing.id, namn, loeysing.url, orgnummer, verksemdId)
            loeysingDAO.update(validated).getOrThrow()
          }
          .fold(
              { ResponseEntity.noContent().build() },
              { exception ->
                logger.error(
                    "Feila då vi skulle oppdatere løysing med id ${loeysing.id}", exception)
                when (exception) {
                  is IllegalArgumentException -> ResponseEntity.badRequest().build()
                  else -> ResponseEntity.internalServerError().build()
                }
              })

  @DeleteMapping("{id}")
  fun delete(@PathVariable id: Int): ResponseEntity<Any> =
      runCatching { loeysingDAO.delete(id) }
          .fold(
              { ResponseEntity.noContent().build() },
              { exception ->
                logger.error("Feila då vi skulle slette løysing med id ${id}", exception)
                ResponseEntity.internalServerError().build()
              })

  @RequestMapping("/expanded", method = [RequestMethod.GET, RequestMethod.POST])
  fun getManyExpanded(
      @RequestParam ids: String?,
      @RequestParam search: String?,
      @RequestParam atTime: String?
  ): ResponseEntity<List<LoeysingExpanded>> {
    val loeysingList =
        getManyBase(atTime, ids, search).mapCatching { loeysingar ->
          loeysingar.map { loeysing -> toLoeysingExpanded(loeysing) }
        }

    return loeysingList.fold(
        { ResponseEntity.ok(it) },
        { exception ->
          logger.error(FEILA_DAA_VI_SKULLE_HENTE_LOEYSINGAR, exception)
          when (exception) {
            is IllegalArgumentException -> ResponseEntity.badRequest().build()
            else -> ResponseEntity.internalServerError().build()
          }
        })
  }

  @RequestMapping("/updatemany", method = [RequestMethod.PUT])
  fun updateMany(ids: List<Int>) {
    ids.forEach { id ->
      loeysingDAO.getLoeysing(id)?.let {
        verksemdDAO.getVerksemdByOrgnummer(it.orgnummer).onSuccess { verksemd ->
          loeysingDAO.update(it.copy(verksemdId = verksemd.id))
        }
      }
    }
  }

  private fun toLoeysingExpanded(loeysing: Loeysing) =
      LoeysingExpanded(loeysing.id, loeysing.namn, loeysing.url, getVerksemd(loeysing))

  private fun getVerksemd(loeysing: Loeysing): Verksemd? {
    return loeysing.verksemdId?.let { verksemdId ->
      return verksemdDAO.getVerksemd(verksemdId).getOrNull()
    }
  }
}
