package no.uutilsynet.testlab2loeysingsregister.sideutval

import java.net.URI
import java.net.URL
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingEntity

class NettsideEntity(type: SideutvalType, loeysing: LoeysingEntity, side: String, id: Int) :
    Sideutval(type, loeysing, sidetype = Sidetype.NETTSIDE, side, id) {
  val url: URL = URI(side).toURL()
}
