package no.uutilsynet.testlab2loeysingsregister.loeysing

import java.net.URL
import no.uutilsynet.testlab2loeysingsregister.verksemd.Verksemd

data class LoeysingExpanded(val id: Int, val namn: String, val url: URL, val verksemd: Verksemd?)
