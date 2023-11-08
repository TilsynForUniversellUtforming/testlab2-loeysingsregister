package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

fun locationForId(id: Int): URI = URI("/v1/loeysing/${id}")

@RestController
@RequestMapping("v1/loeysing")
class LoeysingResource(val loeysingDAO: LoeysingDAO) {
  val logger: Logger = LoggerFactory.getLogger(LoeysingResource::class.java)

  @PostMapping
  fun createLoeysing(@RequestBody body: Map<String, String>) =
      runCatching {
            val id = validateOptionalId(body["id"]).getOrThrow()
            val namn = validateNamn(body["namn"]).getOrThrow()
            val url = validateURL(body["url"]).getOrThrow()
            val orgnummer = validateOrgNummer(body["orgnummer"]).getOrThrow()

            val existingLoeysing = loeysingDAO.findLoeysingByURLAndOrgnummer(url, orgnummer)
            existingLoeysing?.id
                ?: loeysingDAO.createLoeysing(namn, url, orgnummer, id).also {
                  logger.info("lagra ny løysing ($url, $orgnummer)")
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
  fun getLoeysing(@PathVariable id: Int): ResponseEntity<Loeysing> =
      loeysingDAO.getLoeysing(id)?.let { ResponseEntity.ok(it) }
          ?: ResponseEntity.notFound().build()

  @GetMapping
  fun getMany(
      @RequestParam ids: String?,
      @RequestParam search: String?
  ): ResponseEntity<List<Loeysing>> {
    return runCatching {
          when {
            ids != null && search?.isNotBlank() == true -> {
              val idList = validateIdList(ids).getOrThrow()
              loeysingDAO.findLoeysingar(search).filter { it.id in idList }
            }
            search?.isNotBlank() == true -> {
              loeysingDAO.findLoeysingar(search)
            }
            ids != null -> {
              val idList = validateIdList(ids).getOrThrow()
              loeysingDAO.getLoeysingList(idList)
            }
            else -> {
              loeysingDAO.getLoeysingList()
            }
          }
        }
        .fold(
            { ResponseEntity.ok(it) },
            { exception ->
              logger.error("Feila då vi skulle hente løysingar", exception)
              when (exception) {
                is IllegalArgumentException -> ResponseEntity.badRequest().build()
                else -> ResponseEntity.internalServerError().build()
              }
            })
  }

  @PutMapping
  fun update(@RequestBody loeysing: Loeysing): ResponseEntity<Unit> =
      runCatching {
            val namn = validateNamn(loeysing.namn).getOrThrow()
            val orgnummer = validateOrgNummer(loeysing.orgnummer).getOrThrow()
            val validated = Loeysing(loeysing.id, namn, loeysing.url, orgnummer)
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
}
