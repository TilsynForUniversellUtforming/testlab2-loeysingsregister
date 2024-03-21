package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant

data class Verksemd(
    val id: Int,
    val namn: String,
    val organisasjonsnummer: String,
    val institusjonellSektorKode: InstitusjonellSektorKode,
    val naeringskode: Naeringskode,
    val organisasjonsform: Organisasjonsform,
    val fylke: Fylke,
    val kommune: Kommune,
    val postadresse: Postadresse?,
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
    val institusjonellSektorKode: InstitusjonellSektorKode,
    val naeringskode: Naeringskode,
    val organisasjonsform: Organisasjonsform,
    val fylke: Fylke,
    val kommune: Kommune,
    val postadresse: Postadresse?,
    val talTilsette: Int?,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?,
    val underAvviking: Boolean = false
)

data class NyVerksemd(
    val namn: String,
    val organisasjonsnummer: String,
    val institusjonellSektorKode: InstitusjonellSektorKode,
    val naeringskode: Naeringskode,
    val organisasjonsform: Organisasjonsform,
    val fylke: Fylke,
    val kommune: Kommune,
    val postadresse: Postadresse?,
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
      organisasjonsnummer = brregVerksemd.organisasjonsnummer,
      institusjonellSektorKode = brregVerksemd.institusjonellSektorkode,
      naeringskode = brregVerksemd.naeringskode1,
      organisasjonsform = brregVerksemd.organisasjonsform,
      fylke = Fylke("", ""),
      kommune =
          Kommune(
              brregVerksemd.forretningsadresse.kommunenummer,
              brregVerksemd.forretningsadresse.kommune),
      postadresse =
          Postadresse(
              brregVerksemd.forretningsadresse.postnummer,
              brregVerksemd.forretningsadresse.poststed),
      talTilsette = 0,
      forvaltningsnivaa = "",
      tenesteromraade = "",
      underAvviking = false,
      aktiv = true,
      original = 0,
      tidspunkt = Instant.now())
}

data class Postadresse(val postnummer: String?, val poststad: String?)

data class InstitusjonellSektorKode(val kode: String, val beskrivelse: String)

data class Naeringskode(val kode: String, val beskrivelse: String)

data class Organisasjonsform(val kode: String, val omtale: String)

data class Fylke(val fylkesnummer: String, val fylke: String)

data class Kommune(val kommunenummer: String, val kommune: String)
