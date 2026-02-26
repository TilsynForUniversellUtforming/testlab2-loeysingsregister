package no.uutilsynet.testlab2loeysingsregister.sideutval

data class SideutvalLookupResponse(val loeysingId: Int, val sideutvalIdList: List<SideUtvalResponse>)

data class SideUtvalResponse(val sideutvalId: Int, val side: String)
