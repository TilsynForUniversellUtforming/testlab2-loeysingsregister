package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant
import kotlin.reflect.full.memberProperties

data class Verksemd(
    val verksemdId: Int,
    val namn: String,
    val orgnummer: String,
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
    val postnummer: Int,
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
    val fylkesnummer: Int,
    val fylke: String?,
    val kommune: String?,
    val kommunenummer: Int?,
    val postnummer: String?,
    val poststad: String?,
    val talTilsette: Int?,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?
)

data class NyVerksemd(
    val namn: String,
    val orgnummer: String,
    val institusjonellSektorkode: String,
    val institusjonellSektorkodeBeskrivelse: String,
    val naeringskode: String,
    val naeringskodeBeskrivelse: String,
    val organisasjonsformKode: String,
    val organsisasjonsformOmtale: String,
    val fylkesnummer: Int = 0,
    val fylke: String = "",
    val kommune: String,
    val kommunenummer: Int,
    val postnummer: Int,
    val poststad: String,
    val talTilsette: Int,
    val forvaltningsnivaa: String,
    val tenesteromraade: String,
    val aktiv: Boolean = true,
    val original: Int,
    val tidspunkt: Instant = Instant.now()
) {
    constructor(brregVerksemd: BrregVerksemd) : this( namn = brregVerksemd.navn,
        orgnummer = brregVerksemd.organisasjonsnummer,
        institusjonellSektorkode = brregVerksemd.institusjonellSektorkode.kode,
        institusjonellSektorkodeBeskrivelse = brregVerksemd.institusjonellSektorkode.beskrivelse,
        naeringskode = brregVerksemd.naeringskode1.kode,
        naeringskodeBeskrivelse = brregVerksemd.naeringskode1.beskrivelse,
        organisasjonsformKode = brregVerksemd.organisasjonsform.kode,
        organsisasjonsformOmtale = brregVerksemd.organisasjonsform.beskrivelse,
        fylkesnummer = 0,
        fylke = "",
        kommunenummer = brregVerksemd.forretningsadresse.kommunenummer,
        kommune = brregVerksemd.forretningsadresse.kommune,
        postnummer = brregVerksemd.forretningsadresse.postnummer,
        poststad = brregVerksemd.forretningsadresse.poststed,
        talTilsette = brregVerksemd.antallAnsatte,
        forvaltningsnivaa = brregVerksemd.forretningsadresse.kommune,
        tenesteromraade = brregVerksemd.forretningsadresse.kommune,
        aktiv = true,
        original = 1) {}

    }
