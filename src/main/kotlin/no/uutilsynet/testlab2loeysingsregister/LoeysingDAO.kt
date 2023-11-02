package no.uutilsynet.testlab2loeysingsregister

import java.net.URL
import no.uutilsynet.testlab2loeysingsregister.LoeysingDAO.LoeysingParams.getLoeysingListSql
import no.uutilsynet.testlab2loeysingsregister.LoeysingDAO.LoeysingParams.getLoeysingSql
import no.uutilsynet.testlab2loeysingsregister.LoeysingDAO.LoeysingParams.loeysingRowMapper
import org.springframework.dao.support.DataAccessUtils
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class LoeysingDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  object LoeysingParams {
    val getLoeysingListSql = "select id, namn, url, orgnummer from loeysing order by id"
    val getLoeysingSql = "select id, namn, url, orgnummer from loeysing where id = :id order by id"

    val loeysingRowMapper = DataClassRowMapper.newInstance(Loeysing::class.java)
  }

  fun getLoeysing(id: Int): Loeysing? =
      DataAccessUtils.singleResult(
          jdbcTemplate.query(getLoeysingSql, mapOf("id" to id), loeysingRowMapper))

  fun getLoeysingList(): List<Loeysing> = jdbcTemplate.query(getLoeysingListSql, loeysingRowMapper)

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

  fun getLoeysingList(idList: List<Int>): List<Loeysing> {
    return jdbcTemplate.query(
        """
              select id, namn, url, orgnummer
              from loeysing
              where id in (:idList)
          """
            .trimIndent(),
        mapOf("idList" to idList),
        loeysingRowMapper)
  }
}
