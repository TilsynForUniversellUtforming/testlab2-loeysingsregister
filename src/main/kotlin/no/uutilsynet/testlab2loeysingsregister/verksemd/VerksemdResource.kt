package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant
import no.uutilsynet.testlab2loeysingsregister.validateInstant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/verksemd")
class VerksemdResource(val verksemdDAO: VerksemdDAO, val verksemdService: VerksemdService) {
  val logger: Logger = LoggerFactory.getLogger(VerksemdResource::class.java)

  @GetMapping("/{id}")
  fun getVerksemd(@PathVariable id: Int, @RequestParam atTime: String?): ResponseEntity<Verksemd> {
    val instant = atTime?.let { validateInstant(atTime).getOrThrow() } ?: Instant.now()
    return verksemdDAO.getVerksemd(id, instant)?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.notFound().build()
  }

  @GetMapping
  fun getVerksemder(@RequestParam atTime: String?): ResponseEntity<List<Verksemd>> {
    val instant = atTime?.let { validateInstant(atTime).getOrThrow() } ?: Instant.now()
    val verksemder = verksemdDAO.getVerksemder(instant)
    if (verksemder.isEmpty()) {
      return ResponseEntity.noContent().build()
    }
    return ResponseEntity.ok(verksemder)
  }

  @PostMapping
  fun createVerksemd(@RequestBody nyVerksemd: NyVerksemdBase): ResponseEntity<Verksemd> {
    val verksemd = verksemdService.getVerksemdData(nyVerksemd.orgnummer).getOrThrow()
    verksemdDAO
        .createVerksemd(verksemd)
        .also { logger.info("lagra verksemd (${verksemd.namn}, ${verksemd.orgnummer})") }
        .let {
          val nyVerksemd = verksemdDAO.getVerksemd(it.getOrThrow())
          return ResponseEntity.ok(nyVerksemd)
        }
  }

  @PutMapping("/{id}")
  fun updateVerksemd(@RequestBody verksemd: Verksemd): ResponseEntity<Verksemd> {
    verksemdDAO
        .updateVerksemd(verksemd, true)
        .fold(
            {
              return ResponseEntity.ok(verksemd)
            },
            {
              return ResponseEntity.notFound().build()
            })
  }

  @DeleteMapping
  fun deleteVerksemd(@PathVariable id: Int): ResponseEntity<Int> {
    verksemdDAO
        .deleteVerksemd(id)
        .fold(
            {
              return ResponseEntity.ok().build()
            },
            {
              return ResponseEntity.notFound().build()
            })
  }
}
