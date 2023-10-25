package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

fun locationForId(id: Int): URI = URI("/v2/loeysing/${id}")

@RestController
@RequestMapping("v2/loeysing")
class LoeysingResource(val loeysingDAO: LoeysingDAO) {
  val logger = LoggerFactory.getLogger(LoeysingResource::class.java)

  @PostMapping
  fun createLoeysing(@RequestBody external: Loeysing.External) =
      runCatching {
            val namn = validateNamn(external.namn).getOrThrow()
            val url = validateURL(external.url).getOrThrow()
            val orgnummer = validateOrgNummer(external.orgnummer).getOrThrow()

            val existingLoeysing = loeysingDAO.findLoeysingByURLAndOrgnummer(url, orgnummer)
            existingLoeysing?.id
                ?: loeysingDAO.createLoeysing(namn, url, orgnummer).also {
                  logger.info("lagra ny løysing ($url, $orgnummer)")
                }
          }
          .fold(
              { id ->
                val location = locationForId(id)
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
  fun getLoeysing(@PathVariable id: Int): ResponseEntity<Loeysing> =
      loeysingDAO.getLoeysing(id)?.let { ResponseEntity.ok(it) }
          ?: ResponseEntity.notFound().build()

  @GetMapping
  fun getMany(@RequestParam ids: String? = null): ResponseEntity<List<Loeysing>> {
    return runCatching {
          if (ids != null) {
            val idList = validateIdList(ids).getOrThrow()
            loeysingDAO.getLoeysingList(idList)
          } else {
            loeysingDAO.getLoeysingList()
          }
        }
        .fold(
            { ResponseEntity.ok(it) },
            { exception ->
              when (exception) {
                is IllegalArgumentException -> ResponseEntity.badRequest().build()
                else -> ResponseEntity.internalServerError().build()
              }
            })
  }
}
