package no.uutilsynet.testlab2loeysingsregister.dto

data class Nettstad(
    val id: Int,
    val namn: String,
    val nettadresse: String,
    val typeNettstad: String,
    val tenesteomraade: String,
    val typeTenester: String,
    val prioriterAvInteressentar: Boolean,
    val verksemdId: Int
)
