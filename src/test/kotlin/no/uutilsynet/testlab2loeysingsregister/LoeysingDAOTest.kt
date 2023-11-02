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

  @AfterAll
  fun cleanup() {
    loeysingDAO.jdbcTemplate.update(
        "delete from loeysing where namn = :namn", mapOf("namn" to loeysingTestName))
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
    val loeysing = loeysingDAO.getLoeysing(id)
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
    val id1 = createLoeysing()
    val id2 = createLoeysing(url = "https://www.nrk.no/")

    val result1 = loeysingDAO.getLoeysingList(listOf(id1))
    val result2 = loeysingDAO.getLoeysingList(listOf(id1, id2))

    assertThat(result1.map(Loeysing::id)).containsExactly(id1)
    assertThat(result2.map(Loeysing::id)).containsExactly(id1, id2)
  }

  private fun createLoeysing(name: String = loeysingTestName, url: String = loeysingTestUrl) =
      loeysingDAO.createLoeysing(name, URI(url).toURL(), loeysingTestOrgNummer)
}
