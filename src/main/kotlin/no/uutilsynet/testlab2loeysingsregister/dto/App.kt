package no.uutilsynet.testlab2loeysingsregister.dto

data class App(
  val id: Int,
  val namn: String,
  val operativsystem: String,
  val antallNedlastinger: String,
  val versjon: String,
  val adresse: String,
  val tenesteomraade: String,
  val prioriterAvInteressentar: Boolean,
  val verksemdId: Int
)
