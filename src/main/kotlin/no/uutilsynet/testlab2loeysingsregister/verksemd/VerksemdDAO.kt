package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VerksemdDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

  val logger = LoggerFactory.getLogger(VerksemdDAO::class.java)

  val insertVerksemdSql =
      """
            insert into verksemd(namn,
            organisasjonsnummer,
            institusjonell_sektorkode,
            institusjonell_sektorkode_beskrivelse,
            naeringskode,
            naeringskode_beskrivelse,
            organisasjonsform_kode,
            organisasjonsform_omtale,
            fylkesnummer,
            fylke,
            kommunenummer,
            kommune,
            postnummer,
            poststad,
            tal_tilsette,
            forvaltningsnivaa,
            tenesteromraade,
            under_avviking,
            aktiv,
            original,
            tidspunkt)
            values (:namn,
            :organisasjonsnummer,
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
            :under_avviking,
            :aktiv,
            :original,
            :tidspunkt) 
            returning id         
        """
          .trimIndent()

  @Transactional
  fun createVerksemd(verksemd: NyVerksemd): Result<Int> {
    return runCatching {
          val existing = exisitingVerksemd(verksemd.organisasjonsnummer)
          logger.info("Existing verksemd: $existing")

          val params = verksemdParamsMap(verksemd, existing)

          jdbcTemplate.queryForObject(insertVerksemdSql, params, Int::class.java)
              ?: throw Exception("Oppretting av verksemd ${verksemd.organisasjonsnummer} feila")
        }
        .fold(onSuccess = { id -> Result.success(id) }, onFailure = { e -> Result.failure(e) })
  }

  @Transactional
  fun updateVerksemd(verksemd: Verksemd, updateAktiv: Boolean = true): Result<Int> {
    return runCatching { createVerksemd(verksemd.copy(aktiv = updateAktiv).toNyVerksemd()) }
        .fold(
            onSuccess = { id -> Result.success(id.getOrThrow()) },
            onFailure = { e -> Result.failure(e) })
  }

  fun getVerksemd(id: Int, atTime: Instant = Instant.now()): Result<Verksemd> {
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

    val verksemd =
        jdbcTemplate.queryForObject(sql, mapOf("id" to id, "atTime" to Timestamp.from(atTime))) {
            rs,
            _ ->
          verksemdRowmapper(rs)
        }

    return verksemd?.let { Result.success(it) }
        ?: Result.failure(Exception("Verksemd med id $id finst ikkje"))
  }

  fun getVerksemdByOrgnummer(
      organisasjonsnummer: String,
      atTime: Instant = Instant.now()
  ): Result<Verksemd> {
    val sql =
        """
                select *
                from verksemd
                where organisasjonsnummer =:organisasjonsnummer
                and tidspunkt <= :atTime
                order by tidspunkt desc
            """
            .trimIndent()

    val verksemd =
        jdbcTemplate.query(
            sql,
            mapOf(
                "organisasjonsnummer" to organisasjonsnummer,
                "atTime" to Timestamp.from(atTime))) { rs, _ ->
              verksemdRowmapper(rs)
            }

    if (verksemd.isNotEmpty()) {
      return verksemd.first().let { Result.success(it) }
    }
    return Result.failure(
        Exception("Verksemd med organisasjonsnummer $organisasjonsnummer finst ikkje"))
  }

  private fun verksemdRowmapper(rs: ResultSet): Verksemd {
    return Verksemd(
        id = rs.getInt("id"),
        namn = rs.getString("namn"),
        organisasjonsnummer = rs.getString("organisasjonsnummer"),
        institusjonellSektorKode =
            InstitusjonellSektorKode(
                rs.getString("institusjonell_sektorkode"),
                rs.getString("institusjonell_sektorkode_beskrivelse")),
        naeringskode =
            Naeringskode(rs.getString("naeringskode"), rs.getString("naeringskode_beskrivelse")),
        organisasjonsform =
            Organisasjonsform(
                rs.getString("organisasjonsform_kode"), rs.getString("organisasjonsform_omtale")),
        fylke = Fylke(rs.getString("fylkesnummer"), rs.getString("fylke")),
        kommune = Kommune(rs.getString("kommunenummer"), rs.getString("kommune")),
        postadresse = Postadresse(rs.getString("postnummer"), rs.getString("poststad")),
        talTilsette = rs.getInt("tal_tilsette"),
        forvaltningsnivaa = rs.getString("forvaltningsnivaa"),
        tenesteromraade = rs.getString("tenesteromraade"),
        aktiv = rs.getBoolean("aktiv"),
        original = rs.getInt("original"),
        tidspunkt = rs.getTimestamp("tidspunkt").toInstant(),
        underAvviking = rs.getBoolean("under_avviking"))
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

    return jdbcTemplate.query(sql, mapOf("atTime" to Timestamp.from(atTime), "aktiv" to aktiv)) {
        rs,
        _ ->
      verksemdRowmapper(rs)
    }
  }

  @Transactional
  fun deleteVerksemd(id: Int): Result<Int> {
    return getVerksemd(id).mapCatching { updateVerksemd(it, false).getOrThrow() }
  }

  private fun exisitingVerksemd(organisasjonsnummer: String): Int {
    val sql =
        """
            select id 
            from verksemd
            where organisasjonsnummer = :organisasjonsnummer
            order by tidspunkt desc
        """
            .trimIndent()

    return jdbcTemplate
        .queryForList(sql, mapOf("organisasjonsnummer" to organisasjonsnummer), Int::class.java)
        .firstOrNull()
        ?: 0
  }

  private fun verksemdParamsMap(verksemd: NyVerksemd, original: Int) =
      mapOf(
          "namn" to verksemd.namn,
          "organisasjonsnummer" to verksemd.organisasjonsnummer,
          "institusjonell_sektorkode" to verksemd.institusjonellSektorKode.kode,
          "institusjonell_sektorkode_beskrivelse" to verksemd.institusjonellSektorKode.beskrivelse,
          "naeringskode" to verksemd.naeringskode.kode,
          "naeringskode_beskrivelse" to verksemd.naeringskode.beskrivelse,
          "organisasjonsform_kode" to verksemd.organisasjonsform.kode,
          "organsisasjonsform_omtale" to verksemd.organisasjonsform.omtale,
          "fylkesnummer" to verksemd.fylke.fylkesnummer,
          "fylke" to verksemd.fylke.fylke,
          "kommunenummer" to verksemd.kommune.kommunenummer,
          "kommune" to verksemd.kommune.kommune,
          "postnummer" to verksemd.postadresse?.postnummer,
          "poststad" to verksemd.postadresse?.poststad,
          "tal_tilsette" to verksemd.talTilsette,
          "forvaltningsnivaa" to verksemd.forvaltningsnivaa,
          "tenesteromraade" to verksemd.tenesteromraade,
          "under_avviking" to verksemd.underAvviking,
          "aktiv" to verksemd.aktiv,
          "original" to original,
          "tidspunkt" to Timestamp.from(Instant.now()))

  fun Verksemd.toNyVerksemd(): NyVerksemd {
    return NyVerksemd(
        namn = namn,
        organisasjonsnummer = organisasjonsnummer,
        institusjonellSektorKode = institusjonellSektorKode,
        naeringskode = naeringskode,
        organisasjonsform = organisasjonsform,
        fylke = fylke,
        kommune = kommune,
        postadresse = postadresse,
        talTilsette = talTilsette,
        forvaltningsnivaa = forvaltningsnivaa,
        tenesteromraade = tenesteromraade,
        underAvviking = underAvviking,
        aktiv = aktiv,
        original = original,
        tidspunkt = tidspunkt)
  }
}
