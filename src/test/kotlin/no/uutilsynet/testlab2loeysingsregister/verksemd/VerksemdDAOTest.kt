package no.uutilsynet.testlab2loeysingsregister.verksemd

import java.time.Instant
import kotlin.properties.Delegates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VerksemdDAOTest(@Autowired val verksemdDAO: VerksemdDAO) {

  var verksemdId by Delegates.notNull<Int>()
  var originalTimeStamp by Delegates.notNull<Instant>()
  val orgnummer = "123456789"

  @Test
  @Order(1)
  fun createVerksemd() {
    val nyVerksemd =
        NyVerksemd(
            namn = "Testverksemd",
            organisasjonsnummer = orgnummer,
            institusjonellSektorKode = InstitusjonellSektorKode("123", "Testsektor"),
            naeringskode = Naeringskode("123", "Testnaering"), // "123", "Testnaering
            organisasjonsform = Organisasjonsform("123", "Testform"),
            fylke = Fylke("46", "Testfylke"),
            kommune = Kommune("123", "Testkommune"),
            postadresse = Postadresse("123", "Teststad"),
            talTilsette = 123,
            forvaltningsnivaa = "Testnivå",
            tenesteromraade = "Testområde",
            aktiv = true,
            original = 1)

    val response = verksemdDAO.createVerksemd(nyVerksemd)

    verksemdId = response.getOrThrow()

    assertThat(verksemdId).isGreaterThan(0)
  }

  @Test
  @Order(2)
  fun getVerksemd() {
    verksemdDAO.getVerksemd(verksemdId).onSuccess {
      assertEquals(it.namn, "Testverksemd")
      assertEquals(it.organisasjonsnummer, "123456789")
      originalTimeStamp = it.tidspunkt
    }
  }

  @Test
  @Order(3)
  fun updateVerksemd() {
    val gamalVerksemd = verksemdDAO.getVerksemd(verksemdId).getOrThrow()
    val oppdatertVerksemd = gamalVerksemd.copy(namn = "Oppdatert verksemd")
    val nyId = verksemdDAO.updateVerksemd(oppdatertVerksemd).getOrThrow()

    val hentaOppdatering = verksemdDAO.getVerksemd(nyId).getOrThrow()
    verksemdDAO.getVerksemd(verksemdId, originalTimeStamp).getOrThrow()

    assertEquals(gamalVerksemd.organisasjonsnummer, hentaOppdatering.organisasjonsnummer)
    assertThat(hentaOppdatering.tidspunkt).isAfter(originalTimeStamp)
    assertThat(hentaOppdatering.original).isEqualTo(verksemdId)
    verksemdId = nyId
  }

  @Test
  @Order(4)
  fun getVerksemder() {
    val verksemder = verksemdDAO.getVerksemder(Instant.now())

    val filtrertVerksemder = verksemder.filter { it.id == verksemdId }
    assertThat(filtrertVerksemder).hasSize(1)
    assertThat(filtrertVerksemder[0].namn).isEqualTo("Oppdatert verksemd")
  }

  @Test
  @Order(5)
  fun deleteVerksemd() {
    val result = verksemdDAO.deleteVerksemd(verksemdId).getOrThrow()
    assertThat(result).isGreaterThan(0)
    val slettaVerksemd = verksemdDAO.getVerksemdByOrgnummer(orgnummer).getOrThrow()

    val slettaTidspunkt = slettaVerksemd.tidspunkt
    val gamalVersjon = verksemdDAO.getVerksemd(verksemdId).getOrThrow()
    assertThat(slettaVerksemd.aktiv).isEqualTo(false)
    assertThat(gamalVersjon.aktiv).isEqualTo(true)
    val listeUtanSletta = verksemdDAO.getVerksemder(Instant.now()).filter { it.id == verksemdId }

    assertThat(listeUtanSletta).isEmpty()

    val slettaFromOrgnummer = verksemdDAO.getVerksemdByOrgnummer(orgnummer)
    assertThat(slettaFromOrgnummer.isFailure)

    val gamalListe = verksemdDAO.getVerksemder(slettaTidspunkt)

    assertThat(gamalListe.map { it.id }).contains(verksemdId)
  }
}
