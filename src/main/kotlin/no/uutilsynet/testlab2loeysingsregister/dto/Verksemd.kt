package no.uutilsynet.testlab2loeysingsregister.dto

data class Verksemd(
    val id: Int,
    val orgnummer: String,
    val namn: String,
    val orgkode: String,
    val naeringskode: String,
    val naringskodeBeskrivelse: String,
    val sektorkode: String,
    val sektorkodeBeskrivelse: String,
    val orgformkode: String,
    val orgformBeskrivelse: String,
    val talTilsette: String,
    val postnr: String,
    val poststad: String
)
