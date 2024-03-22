package no.uutilsynet.testlab2loeysingsregister.verksemd

data class BrregVerksemd(
    val organisasjonsnummer: String,
    val navn: String,
    val organisasjonsform: Organisasjonsform,
    val postadresse: Postadresse?,
    val naeringskode1: Naeringskode,
    val antallAnsatte: Int,
    val overordnetEnhet: String?,
    val forretningsadresse: Postadresse,
    val institusjonellSektorkode: InstitusjonellSektorkode,
    val underAvviking: Boolean = false
) {

  data class Organisasjonsform(val kode: String, val beskrivelse: String)

  data class Postadresse(
      val postnummer: String,
      val poststed: String,
      val kommune: String,
      val kommunenummer: String
  )

  data class Naeringskode(val kode: String, val beskrivelse: String)

  data class InstitusjonellSektorkode(val kode: String, val beskrivelse: String)
}
