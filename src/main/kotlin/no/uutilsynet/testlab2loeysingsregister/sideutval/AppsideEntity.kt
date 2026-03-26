package no.uutilsynet.testlab2loeysingsregister.sideutval

import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingEntity

class AppsideEntity(
    type: SideutvalType,
    loeysing: LoeysingEntity,
    side: String,
    id: Int,
) : Sideutval(type, loeysing, sidetype = Sidetype.APPSIDE, side, id) {

  val beskrivelse: String = this.side
}
