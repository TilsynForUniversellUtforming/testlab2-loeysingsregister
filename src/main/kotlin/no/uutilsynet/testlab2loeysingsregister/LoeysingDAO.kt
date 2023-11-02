package no.uutilsynet.testlab2loeysingsregister

import java.net.URL
import java.time.Instant
import no.uutilsynet.testlab2loeysingsregister.LoeysingDAO.LoeysingParams.loeysingRowMapper
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LoeysingDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  object LoeysingParams {

    val loeysingRowMapper = DataClassRowMapper.newInstance(Loeysing::class.java)
  }

  private data class PartialLoeysing(
      val id: Int,
      val namn: String?,
      val url: String?,
      val orgnummer: String?,
      val aktiv: Boolean?,
      val original: Int,
      val tidspunkt: Instant
  )

  private fun combine(a: PartialLoeysing, b: PartialLoeysing): PartialLoeysing {
    val earliest = if (a.tidspunkt.isBefore(b.tidspunkt)) a else b
    val latest = if (a.tidspunkt.isAfter(b.tidspunkt)) a else b
    return PartialLoeysing(
        id = earliest.original,
        namn = latest.namn ?: earliest.namn,
        url = latest.url ?: earliest.url,
        orgnummer = latest.orgnummer ?: earliest.orgnummer,
        aktiv = latest.aktiv ?: earliest.aktiv,
        original = latest.original,
        tidspunkt = latest.tidspunkt)
  }

  fun getLoeysing(id: Int): Loeysing? = getLoeysingList(listOf(id)).firstOrNull()

  fun getLoeysingList(idList: List<Int>? = null): List<Loeysing> {
    val whereClause: String =
        if (idList != null) {
          "where original in (:idList)"
        } else {
          ""
        }
    val partials =
        jdbcTemplate.query(
            """
              select id, namn, url, orgnummer, aktiv, original, tidspunkt
              from loeysing
              $whereClause
          """
                .trimIndent(),
            mapOf("idList" to idList),
            DataClassRowMapper.newInstance(PartialLoeysing::class.java))
    return partials
        .groupBy { it.original }
        .map { (_, partials) -> partials.reduce(::combine) }
        .filter { it.aktiv!! }
        .map { Loeysing(it.id, it.namn!!, URL(it.url!!), it.orgnummer!!) }
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
