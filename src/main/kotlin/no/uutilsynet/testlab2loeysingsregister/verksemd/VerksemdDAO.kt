package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.springframework.jdbc.core.DataClassRowMapper
import java.time.Instant
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class VerksemdDAO(val jdbcTemplate: NamedParameterJdbcTemplate) {

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
            :kommunenamn,
            :postnummer,
            :poststad,
            :tal_tilsette,
            :forvaltningsnivaa,
            :tenesteromraade,
            :aktiv,
            :original,
            :tidspunkt)
            
        """
            .trimIndent()

    @Transactional
    fun createVerksemd(verksemd: Verksemd): Int {

        val existing = exisitingVerksemd(verksemd.organisasjonsnummer)


        return jdbcTemplate.update(
            insertVerksemdSql,
            verksemdParamsMap(verksemd, existing,aktiv = true)
        )
    }

    @Transactional
    fun updateVerksemd(verksemd: Verksemd): Int {
        val existing = exisitingVerksemd(verksemd.organisasjonsnummer)

        if(existing != 0) {
            val sql = """
            update verksemd
            set aktiv = :aktiv
            where id=:existing
        """.trimIndent()

            jdbcTemplate.update(sql, mapOf("aktiv" to false,"existing" to existing))
        }
        return createVerksemd(verksemd.copy(original = existing))
    }

    fun getVerksemd(id:Int, atTime: Instant = Instant.now()): Verksemd? {
        val sql = """
            select *
            from verksemd
            where id = :id
            and tidspunkt <= :atTime
            order by tidspunkt desc
            fetch first 1 row only
        """.trimIndent()

        return jdbcTemplate.queryForObject(sql, mapOf("atTime" to atTime), DataClassRowMapper.newInstance(Verksemd::class.java))
    }

    fun getVerksemder(atTime: Instant = Instant.now()): List<Verksemd> {
        val sql = """
            select *
            from verksemd
            where 
            aktiv = true
            and 
            tidspunkt <= :atTime
            order by tidspunkt desc
        """.trimIndent()

        return jdbcTemplate.query(sql, mapOf("atTime" to atTime), DataClassRowMapper.newInstance(Verksemd::class.java))
    }

    @Transactional
    fun deleteVerksemd(id: Int) : Int {
        val last:Verksemd? = getVerksemd(id)
        if(last!=null) {
            return jdbcTemplate.update(insertVerksemdSql, verksemdParamsMap(last, last.original,false))
        }
        return 0
    }

    private fun exisitingVerksemd(orgnummer: String): Int {
        val sql =
            """
            select distinct original
            from verksemd
            where orgnummer = :orgnummer
            order by tidspunkt desc
        """
                .trimIndent()

        return jdbcTemplate
            .queryForList(sql, mapOf("orgnummer" to orgnummer), Int::class.java)
            .firstOrNull() ?: 0
    }

    private fun verksemdParamsMap(verksemd: Verksemd, original: Int, aktiv: Boolean): Map<String, Any> {
        return mapOf(
            "namn" to verksemd.namn,
            "orgnummer" to verksemd.organisasjonsnummer,
            "institusjonell_sektorkode" to verksemd.institusjonellSektorkode,
            "institusjonell_sektorkode_beskrivelse" to verksemd.institusjonellSektorkodeBeskrivelse,
            "naeringskode" to verksemd.naeringskode,
            "naeringskode_beskrivelse" to verksemd.naeringskodeBeskrivelse,
            "organisasjonsform_kode" to verksemd.organisasjonsformKode,
            "organsisasjonsform_omtale" to verksemd.organsisasjonsformOmtale,
            "fylkesnummer" to verksemd.fylkesnummer,
            "fylke" to verksemd.fylke,
            "kommunenummer" to verksemd.kommunenummer,
            "kommunenamn" to verksemd.kommune,
            "postnummer" to verksemd.postnummer,
            "poststad" to verksemd.poststad,
            "tal_tilsette" to verksemd.talTilsette,
            "forvaltningsnivaa" to verksemd.forvaltningsnivaa,
            "tenesteromraade" to verksemd.tenesteromraade,
            "aktiv" to aktiv,
            "original" to original,
            "tidspunkt" to Instant.now()
        )
    }
}
