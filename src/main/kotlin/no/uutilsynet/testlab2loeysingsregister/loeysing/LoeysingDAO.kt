package no.uutilsynet.testlab2loeysingsregister.loeysing

import java.net.URI
import java.net.URL
import java.sql.Timestamp
import java.time.Instant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LoeysingDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  val logger: Logger = LoggerFactory.getLogger(LoeysingDAO::class.java)

  private data class PartialLoeysing(
      val namn: String?,
      val url: String?,
      val orgnummer: String?,
      val aktiv: Boolean?,
      val original: Int,
      val tidspunkt: Instant
  )

  private fun combine(a: PartialLoeysing, b: PartialLoeysing): PartialLoeysing {
    val (earliest, latest) = if (a.tidspunkt < b.tidspunkt) Pair(a, b) else Pair(b, a)
    return PartialLoeysing(
        latest.namn ?: earliest.namn,
        latest.url ?: earliest.url,
        latest.orgnummer ?: earliest.orgnummer,
        latest.aktiv ?: earliest.aktiv,
        latest.original,
        latest.tidspunkt)
  }

  private fun getPartials(
      idList: List<Int>? = null,
      atTime: Instant = Instant.now()
  ): List<PartialLoeysing> {
    val idFilter: String =
        if (idList != null) {
          "original in (:idList)"
        } else {
          "true"
        }
    logger.debug("getPartials")
    logger.debug("idFilter: $idFilter")

    val partials =
        jdbcTemplate.query(
            """
              select namn, url, orgnummer, aktiv, original, tidspunkt
              from loeysing
              where tidspunkt <= :atTime
              and $idFilter
          """
                .trimIndent(),
            mapOf("idList" to idList, "atTime" to Timestamp.from(atTime)),
            DataClassRowMapper.newInstance(PartialLoeysing::class.java))

    logger.debug("Fant partials: {}", partials)

    return partials.groupBy { it.original }.map { (_, partials) -> partials.reduce(::combine) }
  }

  fun getLoeysing(id: Int, atTime: Instant = Instant.now()): Loeysing? =
      getLoeysingList(listOf(id), atTime).firstOrNull()

  fun getLoeysingList(idList: List<Int>? = null, atTime: Instant = Instant.now()): List<Loeysing> =
      getPartials(idList, atTime)
          .filter { it.aktiv!! }
          .map { Loeysing(it.original, it.namn!!, URL(it.url!!), it.orgnummer!!) }

  @Transactional
  fun findLoeysingar(searchTerm: String, atTime: Instant = Instant.now()): List<Loeysing> {
    val search = "%$searchTerm%"
    val ids =
        jdbcTemplate.queryForList(
            """
            select distinct original
            from loeysing
            where lower(namn) like lower(:search)
            or lower(url) like lower(:search)
            or orgnummer like :search
        """
                .trimIndent(),
            mapOf("search" to search),
            Int::class.java)
    return if (ids.isEmpty()) emptyList() else getLoeysingList(ids, atTime)
  }

  @Transactional
  fun createLoeysing(namn: String, url: URL, orgnummer: String): Int {
    logger.debug("createLoeysing")
    val existing = findLoeysingByURLAndOrgnummer(url, orgnummer)
    logger.debug("Fant existing: {}", existing)
    return if (existing == null) {
      logger.debug("Oppretter ny løysing")
      val id = jdbcTemplate.queryForObject(
          """
                  with cte as (
                      select nextval('loeysing_id_seq') as id
                  )
                  insert into loeysing (id, namn, url, orgnummer, aktiv, original, tidspunkt)
                  select id, :namn, :url, :orgnummer, true, id, :tidspunkt
                  from cte
                  returning id
                  """,
          mapOf(
              "namn" to namn,
              "url" to url.toString(),
              "orgnummer" to orgnummer,
              "tidspunkt" to Timestamp.from(Instant.now())),
          Int::class.java)!!
        logger.debug("Opprettet løsying med id: {}", id)
        id
    } else if (existing.aktiv == false) {
      logger.debug("Reaktiverer løysing")
      jdbcTemplate.update(
          """
                insert into loeysing (aktiv, original, tidspunkt)
                values (true, :original, :tidspunkt)
            """
              .trimIndent(),
          mapOf("original" to existing.original, "tidspunkt" to Timestamp.from(Instant.now())))
      existing.original
    } else {
      logger.debug("Løysing er aktiv")
      existing.original
    }
  }

  private fun findLoeysingByURLAndOrgnummer(url: URL, orgnummer: String): PartialLoeysing? {
    logger.debug("findLoeysingByURLAndOrgnummer")
    logger.debug("Finner løysing med url: {} og orgnr: {}", url, orgnummer)
    val sammeOrgnummer =
        jdbcTemplate.queryForList(
            """
                select distinct original
                from loeysing
                where orgnummer = :orgnummer
            """
                .trimIndent(),
            mapOf("orgnummer" to orgnummer),
            Int::class.java)

    logger.debug("Fant løsyinger: {}", sammeOrgnummer)
    return if (sammeOrgnummer.isEmpty()) {
      null
    } else {
      val partials = getPartials(sammeOrgnummer)
      partials.find { partial -> sameURL(URI(partial.url!!).toURL(), url) }
    }
  }

  @Transactional
  fun update(updated: Loeysing): Result<Unit> = runCatching {
    val latest =
        getLoeysing(updated.id)
            ?: throw IllegalArgumentException("Fant ikke løysing med id ${updated.id}")
    val diff = diff(latest, updated)
    jdbcTemplate.update(
        """
          insert into loeysing (namn, url, orgnummer, original, tidspunkt)
          values (:namn, :url, :orgnummer, :original, :tidspunkt)
        """
            .trimIndent(),
        mapOf(
            "namn" to diff.namn,
            "url" to diff.url?.toString(),
            "orgnummer" to diff.orgnummer,
            "original" to latest.id,
            "tidspunkt" to Timestamp.from(Instant.now())))
  }

  fun delete(id: Int) {
    jdbcTemplate.update(
        """
              insert into loeysing (aktiv, original, tidspunkt)
              values (false, :id, :tidspunkt)
          """
            .trimIndent(),
        mapOf("id" to id, "tidspunkt" to Timestamp.from(Instant.now())))
  }
}
