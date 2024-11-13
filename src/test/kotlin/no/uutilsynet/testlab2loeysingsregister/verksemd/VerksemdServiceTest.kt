package no.uutilsynet.testlab2loeysingsregister.verksemd

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerksemdServiceTest() {

  val verksemdService = TestVerksemdService()

  @Test
  fun brregtoNyVerksemd() {
    val orgnummer = "991825827"

    val result = verksemdService.getVerksemdData(orgnummer)
    assert(result.isSuccess)
    val verksemd = result.getOrThrow()
    assert(verksemd.namn == "DIGITALISERINGSDIREKTORATET")
    assert(verksemd.naeringskode.kode == "84.110")
    assert(verksemd.institusjonellSektorKode.kode == "6100")
  }

  @Test
  fun getBrregDataNotFound() {
    val orgnummer = "123456789"

    val result = verksemdService.getBrregData(orgnummer)
    assert(result.isFailure)
  }
}

class TestVerksemdService :
    VerksemdService(BrregRegisterProperties("http://localhost:8080"), RestTemplateBuilder()) {
  override fun getBrregData(orgnummer: String): Result<BrregVerksemd> {
    if (orgnummer != "991825827")
        return Result.failure(Exception("Fant ikkje verksemd med orgnummer $orgnummer"))

    return Result.success(
        BrregVerksemd(
            "991825827",
            "DIGITALISERINGSDIREKTORATET",
            Organisasjonsform("84.110", "Organisasjonsledd"),
            BrregVerksemd.Postadresse("0114", "Oslo", "Oslo", "0301"),
            Naeringskode("84.110", "Generell offentlig administrasjon"),
            390,
            "932384469",
            BrregVerksemd.Postadresse("0114", "Oslo", "Oslo", "0301"),
            InstitusjonellSektorKode("6100", "Statlig forvaltning")))
  }
}
