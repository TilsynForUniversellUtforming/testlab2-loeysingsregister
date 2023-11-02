package no.uutilsynet.testlab2loeysingsregister

import java.net.URL
import kotlin.reflect.full.memberProperties

data class Loeysing(val id: Int, val namn: String, val url: URL, val orgnummer: String)

data class LoeysingDiff(val id: Int?, val namn: String?, val url: URL?, val orgnummer: String?)

fun diff(a: Loeysing, b: Loeysing): LoeysingDiff =
    Loeysing::class
        .memberProperties
        .filter { it.get(a).toString() != it.get(b).toString() }
        .associate { it.name to it.get(b) }
        .let {
          LoeysingDiff(
              it["id"] as Int?,
              it["namn"] as String?,
              it["url"] as URL?,
              it["orgnummer"] as String?)
        }
