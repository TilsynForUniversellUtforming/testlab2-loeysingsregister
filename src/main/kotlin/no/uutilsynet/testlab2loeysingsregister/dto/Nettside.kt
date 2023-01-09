package no.uutilsynet.testlab2loeysingsregister.dto

data class Nettside(
    val url: String,
    val sideType: String,
    val prosess: String,
    val typeTeneste: String,
    val nettstadId: Int
)
