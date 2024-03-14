package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant
import kotlin.reflect.full.memberProperties

data class Verksemd(
    val verksemdId: Int,
    val namn: String,
    val organisasjonsnummer: String,
    val institusjonellSektorkode: String,
    val institusjonellSektorkodeBeskrivelse: String,
    val naeringskode: String,
    val naeringskodeBeskrivelse: String,
    val organisasjonsformKode: String,
    val organsisasjonsformOmtale: String,
    val fylkesnummer: Int,
    val fylke: String,
    val kommune: String,
    val kommunenummer: Int,
    val postnummer: String,
    val poststad: String,
    val talTilsette: Int,
    val forvaltningsnivaa: String,
    val tenesteromraade: String,
    val aktiv: Boolean = true,
    val original: Int,
    val tidspunkt: Instant = Instant.now(),

)

data class VerksemdDiff(
    val namn: String?,
    val organisasjonsnummer: String?,
    val institusjonellSektorkode: String?,
    val institusjonellSektorkodeBeskrivelse: String?,
    val naeringskode: String?,
    val naeringskodeBeskrivelse: String?,
    val organisasjonsformKode: String?,
    val organsisasjonsformOmtale: String?,
    val fylkesnummer: Int?,
    val fylke: String?,
    val kommune: String?,
    val kommunenummer: Int?,
    val postnummer: String?,
    val poststad: String?,
    val talTilsette: Int?,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?
)

fun diff(a: Verksemd, b: Verksemd): VerksemdDiff =
    Verksemd::class
        .memberProperties
        .filter { it.get(a).toString() != it.get(b).toString() }
        .associate { it.name to it.get(b) }
        .let {
          VerksemdDiff(
              it["namn"] as String?,
              it["organisasjonsnummer"] as String?,
              it["institusjonellSektorkode"] as String?,
              it["institusjonellSektorkodeBeskrivelse"] as String?,
              it["naeringskode"] as String?,
              it["naeringskodeBeskrivelse"] as String?,
              it["organisasjonsformKode"] as String?,
              it["organsisasjonsformOmtale"] as String?,
              it["fylkesnummer"] as Int?,
              it["fylke"] as String?,
              it["kommune"] as String?,
              it["kommunenummer"] as Int?,
              it["postnummer"] as String?,
              it["poststad"] as String?,
              it["talTilsette"] as Int?,
              it["forvaltningsnivaa"] as String?,
              it["tenesteromraade"] as String?)
        }
