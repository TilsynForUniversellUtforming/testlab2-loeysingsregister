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
    val institusjonellSektorkode: InstitusjonellSektorKode,
    val underAvviking: Boolean = false
) {

  data class Postadresse(
      val postnummer: String,
      val poststed: String,
      val kommune: String,
      val kommunenummer: String
  )
}
