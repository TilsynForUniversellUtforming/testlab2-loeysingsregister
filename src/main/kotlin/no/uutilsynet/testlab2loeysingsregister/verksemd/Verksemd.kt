package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant

data class Verksemd(
    val id: Int,
    val namn: String,
    val orgnummer: String,
    val institusjonellSektorkode: String,
    val institusjonellSektorkodeBeskrivelse: String,
    val naeringskode: String,
    val naeringskodeBeskrivelse: String,
    val organisasjonsformKode: String,
    val organsisasjonsformOmtale: String,
    val fylkesnummer: String,
    val fylke: String,
    val kommune: String,
    val kommunenummer: String,
    val postnummer: String?,
    val poststad: String?,
    val talTilsette: Int,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?,
    val aktiv: Boolean = true,
    val original: Int,
    val tidspunkt: Instant = Instant.now(),
    val underAvviking: Boolean = false
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
    val tenesteromraade: String?,
    val underAvviking: Boolean = false
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
    val fylkesnummer: String,
    val fylke: String = "",
    val kommune: String,
    val kommunenummer: String,
    val postnummer: String?,
    val poststad: String?,
    val talTilsette: Int,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?,
    val underAvviking: Boolean = false,
    val aktiv: Boolean = true,
    val original: Int,
    val tidspunkt: Instant = Instant.now(),
) {
  constructor(
      brregVerksemd: BrregVerksemd
  ) : this(
      namn = brregVerksemd.navn,
      orgnummer = brregVerksemd.organisasjonsnummer,
      institusjonellSektorkode = brregVerksemd.institusjonellSektorkode.kode,
      institusjonellSektorkodeBeskrivelse = brregVerksemd.institusjonellSektorkode.beskrivelse,
      naeringskode = brregVerksemd.naeringskode1.kode,
      naeringskodeBeskrivelse = brregVerksemd.naeringskode1.beskrivelse,
      organisasjonsformKode = brregVerksemd.organisasjonsform.kode,
      organsisasjonsformOmtale = brregVerksemd.organisasjonsform.beskrivelse,
      fylkesnummer = "",
      fylke = "",
      kommunenummer = brregVerksemd.forretningsadresse.kommunenummer.toString(),
      kommune = brregVerksemd.forretningsadresse.kommune,
      postnummer = brregVerksemd.postadresse?.postnummer.toString(),
      poststad = brregVerksemd.postadresse?.poststed.toString(),
      talTilsette = brregVerksemd.antallAnsatte,
      forvaltningsnivaa = null,
      tenesteromraade = null,
      aktiv = true,
      original = 1) {}
}
