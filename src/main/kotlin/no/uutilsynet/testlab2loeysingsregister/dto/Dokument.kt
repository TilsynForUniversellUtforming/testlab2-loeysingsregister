package no.uutilsynet.testlab2loeysingsregister.dto

data class Dokument(
    val id: Int,
    val url: String,
    val typeTeneste: String,
    val prosess: String,
    val verksemdId: Int
)
