package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoeysingDAOTest(
    @Autowired val loeysingDAO: LoeysingDAO,
) {

  val loeysingTestName = UUID.randomUUID().toString()
  val loeysingTestUrl = "https://www.example.com/"
  val loeysingTestOrgNummer = "000000000"

  val idsToBeDeleted = mutableListOf<Int>()

  @AfterAll
  fun cleanup() {
    loeysingDAO.jdbcTemplate.update(
        "delete from loeysing where original in (:ids)", mapOf("ids" to idsToBeDeleted))
  }

  @Test
  @DisplayName("Skal hente løsning fra DAO")
  fun getLoeysing() {
    val id = createLoeysing()
    val loeysing = loeysingDAO.getLoeysing(id)
    assertThat(loeysing?.namn).isEqualTo(loeysingTestName)
    assertThat(loeysing?.url?.toString()).isEqualTo(loeysingTestUrl)
    assertThat(loeysing?.orgnummer).isEqualTo(loeysingTestOrgNummer)
  }

  @Test
  @DisplayName("Skal hente løsningliste fra DAO")
  fun getLoeysingList() {
    val id = createLoeysing()
    val loeysing = loeysingDAO.getLoeysing(id)!!

    val list = loeysingDAO.getLoeysingList()

    assertThat(list).contains(loeysing)
  }

  @Test
  @DisplayName("Skal opprette løsning i DAO")
  fun insertLoeysing() {
    val id = assertDoesNotThrow { createLoeysing() }
    val loeysing = loeysingDAO.getLoeysing(id)
    assertThat(loeysing).isNotNull
  }

  @Test
  @DisplayName(
      "når det finnes to løysingar i databasen, så skal vi kunne hente en eller begge med getLoeysingList")
  fun getLoeysingListTest() {
    val namn1 = UUID.randomUUID().toString()
    val namn2 = UUID.randomUUID().toString()
    val id1 = createLoeysing(name = namn1)
    val id2 = createLoeysing(name = namn2)

    val result1 = loeysingDAO.getLoeysingList(listOf(id1))
    val result2 = loeysingDAO.getLoeysingList(listOf(id1, id2))

    assertThat(result1.map(Loeysing::id)).containsExactly(id1)
    assertThat(result1.map(Loeysing::namn)).containsExactly(namn1)
    assertThat(result2.map(Loeysing::id)).containsExactly(id1, id2)
    assertThat(result2.map(Loeysing::namn)).containsExactly(namn1, namn2)
  }

  @Test
  @DisplayName(
      "når det finnes ei løysing i databasen, og vi oppdaterer den, så skal vi få den oppdaterte versjonen når vi leser den ut igjen")
  fun oppdaterLoeysing() {
    val id = createLoeysing()
    val loeysing = loeysingDAO.getLoeysing(id)!!
    val nyttNamn = UUID.randomUUID().toString()
    val updated = loeysing.copy(namn = nyttNamn)

    loeysingDAO.update(updated)
    val readUpdated = loeysingDAO.getLoeysing(id)!!

    assertThat(readUpdated.namn).isEqualTo(nyttNamn)
  }

  @Test
  @DisplayName("når vi slettar ei løysing, så skal den ikkje lenger vere tilgjengeleg")
  fun slettaLoeysing() {
    val id = createLoeysing()
    loeysingDAO.delete(id)
    val loeysing = loeysingDAO.getLoeysing(id)
    assertThat(loeysing).isNull()
  }

  /** Lager ei ny løysing med ei oppdatering. */
  private fun createLoeysing(name: String = loeysingTestName, url: String = loeysingTestUrl): Int {
    val id =
        loeysingDAO.createLoeysing("to be updated", URI(url).toURL(), loeysingTestOrgNummer).also {
          idsToBeDeleted += it
        }
    val loeysing = loeysingDAO.getLoeysing(id)!!
    loeysingDAO.update(loeysing.copy(namn = name))
    return id
  }
}
