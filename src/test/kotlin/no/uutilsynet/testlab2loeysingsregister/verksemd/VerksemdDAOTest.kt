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
            orgnummer = orgnummer,
            institusjonellSektorkode = "123",
            institusjonellSektorkodeBeskrivelse = "Testsektor",
            naeringskode = "123",
            naeringskodeBeskrivelse = "Testnaering",
            organisasjonsformKode = "123",
            organsisasjonsformOmtale = "Testform",
            fylkesnummer = "46",
            fylke = "Testfylke",
            kommunenummer = "4640",
            kommune = "Testkommune",
            postnummer = "123",
            poststad = "Teststad",
            talTilsette = 123,
            forvaltningsnivaa = "Testnivå",
            tenesteromraade = "Testområde",
            aktiv = true,
            original = 1)

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
  @Order(3)
  fun updateVerksemd() {
    val gamalVerksemd =
        verksemdDAO.getVerksemd(verksemdId) ?: throw IllegalStateException("Verksemden finst ikkje")
    val oppdatertVerksemd = gamalVerksemd.copy(namn = "Oppdatert verksemd")
    val nyId = verksemdDAO.updateVerksemd(oppdatertVerksemd).getOrThrow()

    val hentaOppdatering = verksemdDAO.getVerksemd(nyId)
    val gamalVersjon = verksemdDAO.getVerksemd(verksemdId, originalTimeStamp)

    assertEquals(gamalVerksemd.orgnummer, hentaOppdatering?.orgnummer)
    assertThat(hentaOppdatering?.tidspunkt).isAfter(originalTimeStamp)
    assertThat(hentaOppdatering?.original).isEqualTo(verksemdId)
    assertThat(gamalVersjon?.aktiv).isEqualTo(false)
    verksemdId = nyId
  }

  @Test
  @Order(4)
  fun getVerksemder() {
    val verksemder = verksemdDAO.getVerksemder(Instant.now())

    val filtrertVerksemder = verksemder.filter { it.id == verksemdId }
    assertThat(filtrertVerksemder).hasSize(1)
    assertThat(filtrertVerksemder.get(0).namn).isEqualTo("Oppdatert verksemd")
  }

  @Test
  @Order(5)
  fun deleteVerksemd() {
    verksemdDAO.deleteVerksemd(verksemdId).getOrThrow()
    val slettaVerksemd = verksemdDAO.getVerksemdByOrgnummer(orgnummer)

    val slettaTidspunkt = slettaVerksemd?.tidspunkt ?: Instant.now()
    val gamalVersjon = verksemdDAO.getVerksemd(verksemdId)
    assertThat(slettaVerksemd?.aktiv).isEqualTo(false)
    assertThat(gamalVersjon?.aktiv).isEqualTo(true)
    val listeUtanSletta = verksemdDAO.getVerksemder(Instant.now()).filter { it.id == verksemdId }

    assertThat(listeUtanSletta).isEmpty()

    val slettaFromOrgnummer = verksemdDAO.getVerksemdByOrgnummer(orgnummer)
    assertThat(slettaFromOrgnummer == null)

    val gamalListe = verksemdDAO.getVerksemder(slettaTidspunkt)

    assertThat(gamalListe).hasSize(1)
  }
}
