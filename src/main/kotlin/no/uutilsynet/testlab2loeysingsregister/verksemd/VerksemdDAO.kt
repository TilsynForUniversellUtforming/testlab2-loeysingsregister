package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.sql.Timestamp
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VerksemdDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  val logger = LoggerFactory.getLogger(VerksemdDAO::class.java)

  val insertVerksemdSql =
      """
            insert into verksemd(namn,
            orgnummer,
            institusjonell_sektorkode,
            institusjonell_sektorkode_beskrivelse,
            naeringskode,
            naeringskode_beskrivelse,
            organisasjonsform_kode,
            organsisasjonsform_omtale,
            fylkesnummer,
            fylke,
            kommunenummer,
            kommune,
            postnummer,
            poststad,
            tal_tilsette,
            forvaltningsnivaa,
            tenesteromraade,
            aktiv,
            original,
            tidspunkt)
            values (:namn,
            :orgnummer,
            :institusjonell_sektorkode,
            :institusjonell_sektorkode_beskrivelse,
            :naeringskode,
            :naeringskode_beskrivelse,
            :organisasjonsform_kode,
            :organsisasjonsform_omtale,
            :fylkesnummer,
            :fylke,
            :kommunenummer,
            :kommune,
            :postnummer,
            :poststad,
            :tal_tilsette,
            :forvaltningsnivaa,
            :tenesteromraade,
            :aktiv,
            :original,
            :tidspunkt) 
            returning id         
        """
          .trimIndent()

  @Transactional
  fun createVerksemd(verksemd: NyVerksemd): Result<Int> {
    return runCatching {
          val existing = exisitingVerksemd(verksemd.orgnummer)
          logger.info("Existing verksemd: $existing")

          val params = verksemdParamsMap(verksemd, existing)

          jdbcTemplate.queryForObject(insertVerksemdSql, params, Int::class.java)
              ?: throw Exception("Oppretting av verksemd ${verksemd.orgnummer} feila")
        }
        .fold(onSuccess = { id -> Result.success(id) }, onFailure = { e -> Result.failure(e) })
  }

  @Transactional
  fun updateVerksemd(verksemd: Verksemd, updateAktiv: Boolean = true): Result<Int> {
    return runCatching {
          val existing = exisitingVerksemd(verksemd.orgnummer)

          if (existing != 0) {
            val sql =
                """
            update verksemd
            set aktiv = :aktiv
            where id=:existing
        """
                    .trimIndent()

            jdbcTemplate.update(sql, mapOf("aktiv" to !updateAktiv, "existing" to existing))
          }
          createVerksemd(verksemd.copy(original = existing, aktiv = updateAktiv).toNyVerksemd())
        }
        .fold(
            onSuccess = { id -> Result.success(id.getOrThrow()) },
            onFailure = { e -> Result.failure(e) })
  }

  fun getVerksemd(id: Int, atTime: Instant = Instant.now()): Verksemd? {
    val sql =
        """
            select *
            from verksemd
            where id = :id
            and tidspunkt <= :atTime
            order by tidspunkt desc
            fetch first 1 row only
        """
            .trimIndent()

    return jdbcTemplate.queryForObject(
        sql,
        mapOf("id" to id, "atTime" to Timestamp.from(atTime)),
        DataClassRowMapper.newInstance(Verksemd::class.java))
  }

  fun getVerksemdByOrgnummer(orgnummer: String, atTime: Instant = Instant.now()): Verksemd? {
    val sql =
        """
                select *
                from verksemd
                where orgnummer =:orgnummer
                and tidspunkt <= :atTime
                order by tidspunkt desc
                fetch first 1 row only
            """
            .trimIndent()

    return jdbcTemplate.queryForObject(
        sql,
        mapOf("orgnummer" to orgnummer, "atTime" to Timestamp.from(atTime)),
        DataClassRowMapper.newInstance(Verksemd::class.java))
  }

  fun getVerksemder(atTime: Instant = Instant.now(), aktiv: Boolean = true): List<Verksemd> {
    val sql =
        """
            select *
            from verksemd
            where id not in (
            select distinct original 
				from verksemd  
				where tidspunkt < :atTime)
            and
            aktiv = :aktiv
            and 
            tidspunkt < :atTime
            order by tidspunkt desc
        """
            .trimIndent()

    return jdbcTemplate.query(
        sql,
        mapOf("atTime" to Timestamp.from(atTime), "aktiv" to aktiv),
        DataClassRowMapper.newInstance(Verksemd::class.java))
  }

  @Transactional
  fun deleteVerksemd(id: Int): Result<Int> {
    val last: Verksemd? = getVerksemd(id)
    if (last != null) {
      return Result.success(updateVerksemd(last, false).fold({ it }, { throw it }))
    }
    return Result.failure(Exception("Verksemd med id $id finst ikkje"))
  }

  private fun exisitingVerksemd(orgnummer: String): Int {
    val sql =
        """
            select id 
            from verksemd
            where orgnummer = :orgnummer
            order by tidspunkt desc
        """
            .trimIndent()

    return jdbcTemplate
        .queryForList(sql, mapOf("orgnummer" to orgnummer), Int::class.java)
        .firstOrNull()
        ?: 0
  }

  private fun verksemdParamsMap(verksemd: NyVerksemd, original: Int): Map<String, Any> {
    return mapOf(
        "namn" to verksemd.namn,
        "orgnummer" to verksemd.orgnummer,
        "institusjonell_sektorkode" to verksemd.institusjonellSektorkode,
        "institusjonell_sektorkode_beskrivelse" to verksemd.institusjonellSektorkodeBeskrivelse,
        "naeringskode" to verksemd.naeringskode,
        "naeringskode_beskrivelse" to verksemd.naeringskodeBeskrivelse,
        "organisasjonsform_kode" to verksemd.organisasjonsformKode,
        "organsisasjonsform_omtale" to verksemd.organsisasjonsformOmtale,
        "fylkesnummer" to verksemd.fylkesnummer,
        "fylke" to verksemd.fylke,
        "kommunenummer" to verksemd.kommunenummer,
        "kommune" to verksemd.kommune,
        "postnummer" to verksemd.postnummer,
        "poststad" to verksemd.poststad,
        "tal_tilsette" to verksemd.talTilsette,
        "forvaltningsnivaa" to verksemd.forvaltningsnivaa,
        "tenesteromraade" to verksemd.tenesteromraade,
        "aktiv" to verksemd.aktiv,
        "original" to original,
        "tidspunkt" to Timestamp.from(Instant.now()))
  }

  fun Verksemd.toNyVerksemd(): NyVerksemd {
    return NyVerksemd(
        namn = namn,
        orgnummer = orgnummer,
        institusjonellSektorkode = institusjonellSektorkode,
        institusjonellSektorkodeBeskrivelse = institusjonellSektorkodeBeskrivelse,
        naeringskode = naeringskode,
        naeringskodeBeskrivelse = naeringskodeBeskrivelse,
        organisasjonsformKode = organisasjonsformKode,
        organsisasjonsformOmtale = organsisasjonsformOmtale,
        fylkesnummer = fylkesnummer,
        fylke = fylke,
        kommunenummer = kommunenummer,
        kommune = kommune,
        postnummer = postnummer,
        poststad = poststad,
        talTilsette = talTilsette,
        forvaltningsnivaa = forvaltningsnivaa,
        tenesteromraade = tenesteromraade,
        aktiv = aktiv,
        original = original,
        tidspunkt = tidspunkt)
  }
}
