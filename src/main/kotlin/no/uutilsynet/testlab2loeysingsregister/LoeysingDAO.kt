package no.uutilsynet.testlab2loeysingsregister

import java.net.URL
import java.sql.Timestamp
import java.time.Instant
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LoeysingDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  private val loeysingRowMapper = DataClassRowMapper.newInstance(Loeysing::class.java)

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

  fun getLoeysing(id: Int, atTime: Instant = Instant.now()): Loeysing? =
      getLoeysingList(listOf(id), atTime).firstOrNull()

  fun getLoeysingList(idList: List<Int>? = null, atTime: Instant = Instant.now()): List<Loeysing> {
    val idFilter: String =
        if (idList != null) {
          "original in (:idList)"
        } else {
          "true"
        }
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
    return partials
        .groupBy { it.original }
        .map { (_, partials) -> partials.reduce(::combine) }
        .filter { it.aktiv!! }
        .map { Loeysing(it.original, it.namn!!, URL(it.url!!), it.orgnummer!!) }
  }

  @Transactional
  fun findLoeysingar(searchTerm: String): List<Loeysing> {
    val search = "%$searchTerm%"
    val ids =
        jdbcTemplate.queryForList(
            """
            select distinct original
            from loeysing
            where lower(namn) like lower(:search)
            or orgnummer like :search
        """
                .trimIndent(),
            mapOf("search" to search),
            Int::class.java)
    return if (ids.isEmpty()) emptyList() else getLoeysingList(ids)
  }

  @Transactional
  fun createLoeysing(namn: String, url: URL, orgnummer: String, id: Int? = null): Int =
      if (id != null) {
        jdbcTemplate.queryForObject(
            "insert into loeysing (id, namn, url, orgnummer, aktiv, original, tidspunkt) values (:id, :namn, :url, :orgnummer, true, :id, now()) returning id",
            mapOf("id" to id, "namn" to namn, "url" to url.toString(), "orgnummer" to orgnummer),
            Int::class.java)!!
      } else {
        jdbcTemplate.queryForObject(
            """
                with cte as (
                    select nextval('loeysing_id_seq') as id
                )
                insert into loeysing (id, namn, url, orgnummer, aktiv, original, tidspunkt)
                select id, :namn, :url, :orgnummer, true, id, now()
                from cte
                returning id
                """,
            mapOf("namn" to namn, "url" to url.toString(), "orgnummer" to orgnummer),
            Int::class.java)!!
      }

  fun findLoeysingByURLAndOrgnummer(url: URL, orgnummer: String): Loeysing? {
    val sammeOrgnummer =
        jdbcTemplate.query(
            """
                select id, namn, url, orgnummer
                from loeysing
                where orgnummer = :orgnummer
            """
                .trimIndent(),
            mapOf("orgnummer" to orgnummer),
            loeysingRowMapper)
    return sammeOrgnummer.find { loeysing -> sameURL(loeysing.url, url) }
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
          values (:namn, :url, :orgnummer, :original, now())
        """
            .trimIndent(),
        mapOf(
            "namn" to diff.namn,
            "url" to diff.url?.toString(),
            "orgnummer" to diff.orgnummer,
            "original" to latest.id))
  }

  fun delete(id: Int) {
    jdbcTemplate.update(
        """
              insert into loeysing (aktiv, original, tidspunkt)
              values (false, :id, now())
          """
            .trimIndent(),
        mapOf("id" to id))
  }
}
