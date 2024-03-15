package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant
import kotlin.properties.Delegates

@SpringBootTest
class VerksemdDAOTest(@Autowired val verksemdDAO: VerksemdDAO) {

    var verksemdId by Delegates.notNull<Int>()
    var originalTimeStamp by Delegates.notNull<Instant>()

    @Test
    @Order(1)
    fun createVerksemd() {
        val nyVerksemd =
            NyVerksemd(
                namn = "Testverksemd",
                orgnummer = "123456789",
                institusjonellSektorkode = "123",
                institusjonellSektorkodeBeskrivelse = "Testsektor",
                naeringskode = "123",
                naeringskodeBeskrivelse = "Testnæring",
                organisasjonsformKode = "123",
                organsisasjonsformOmtale = "Testform",
                fylkesnummer = 46,
                fylke = "Testfylke",
                kommunenummer = 4640,
                kommune = "Testkommune",
                postnummer = 123,
                poststad = "Teststad",
                talTilsette = 123,
                forvaltningsnivaa = "Testnivå",
                tenesteromraade = "Testområde",
                aktiv = true,
                original = 1
            )

        val response = verksemdDAO.createVerksemd(nyVerksemd)
        assertTrue(response.isSuccess)
        verksemdId = response.getOrThrow()
    }

    @Test
    @Order(2)
    fun getVerksemd() {
        verksemdDAO.getVerksemd(verksemdId)?.let {
            assertEquals(it.namn, "Testverksemd")
            assertEquals(it.orgnummer, "123456789")
            originalTimeStamp = it.tidspunkt
        }
    }

    @Test
    fun updateVerksemd() {
    }



    @Test
    fun getVerksemder() {
    }

    @Test
    fun deleteVerksemd() {
    }
}

