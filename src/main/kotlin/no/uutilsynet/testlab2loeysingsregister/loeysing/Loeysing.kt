package no.uutilsynet.testlab2loeysingsregister.loeysing

import java.net.URL
import kotlin.reflect.full.memberProperties

data class Loeysing(
    val id: Int,
    val namn: String,
    val url: URL,
    val orgnummer: String,
    val verksemdId: Int?
)

data class LoeysingDiff(
    val namn: String?,
    val url: URL?,
    val orgnummer: String?,
    val verksemdId: Int?
)

fun diff(a: Loeysing, b: Loeysing): LoeysingDiff =
    Loeysing::class
        .memberProperties
        .filter { it.get(a).toString() != it.get(b).toString() }
        .associate { it.name to it.get(b) }
        .let {
          LoeysingDiff(
              it["namn"] as String?,
              it["url"] as URL?,
              it["orgnummer"] as String?,
              it["verksemdId"] as Int?)
        }
