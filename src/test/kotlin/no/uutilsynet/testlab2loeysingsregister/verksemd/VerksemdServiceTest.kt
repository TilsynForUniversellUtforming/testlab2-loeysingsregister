package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerksemdServiceTest(@Autowired val verksemdService: VerksemdService) {

    @Disabled
    @Test
    fun brregtoNyVerksemd() {
        val orgnummer = "991825827"

        val result = verksemdService.getVerksemdData(orgnummer)
        assert(result.isSuccess)
        val verksemd = result.getOrThrow()
        assert(verksemd.namn == "DIGITALISERINGSDIREKTORATET")
        assert(verksemd.naeringskode == "84.110")
        assert(verksemd.institusjonellSektorkode == "6100")
    }

    @Disabled
    @Test
    fun getBrregDataNotFound() {
        val orgnummer = "123456789"

        val result = verksemdService.getBrregData(orgnummer)
        assert(result.isFailure)
    }

}