package no.uutilsynet.testlab2loeysingsregister.dto

data class Automat(
    val id: Int,
    val namn: String,
    val geografiskPlassering: String,
    val verksemdId: Int
)
