package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import java.time.Instant
import java.util.*
import no.uutilsynet.testlab2loeysingsregister.loeysing.Loeysing
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingDAO
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoeysingDAOTest(@Autowired val loeysingDAO: LoeysingDAO) {

  val idsToBeDeleted = mutableListOf<Int>()

  @AfterAll
  fun cleanup() {
    loeysingDAO.jdbcTemplate.update(
        "delete from loeysing where original in (:ids)", mapOf("ids" to idsToBeDeleted))
  }

  @Test
  @DisplayName("Skal hente løsning fra DAO")
  fun getLoeysing() {
    val name = UUID.randomUUID().toString()
    val url = "https://www.$name.com"
    val orgnummer = generateOrgnummer()
    val id = createLoeysing(name, url, orgnummer)
    val loeysing = loeysingDAO.getLoeysing(id)
    assertThat(loeysing?.namn).isEqualTo(name)
    assertThat(loeysing?.url?.toString()).isEqualTo(url)
    assertThat(loeysing?.orgnummer).isEqualTo(orgnummer)
  }

  @Test
  @DisplayName(
      "når ei løysing har blitt oppdatert, så skal vi kunne hente ein tidligare versjon ved å oppgi tidspunkt")
  fun tidligareVersjon() {
    val beforeCreation = Instant.now()

    val id = createLoeysing()
    val loeysing = loeysingDAO.getLoeysing(id)!!
    val namn = loeysing.namn
    val afterCreation = Instant.now()

    val updated = loeysing.copy(namn = "updated")
    loeysingDAO.update(updated)

    val latest = loeysingDAO.getLoeysing(id)!!
    assertThat(latest.namn).isEqualTo("updated")

    val firstVersion = loeysingDAO.getLoeysing(id, afterCreation)!!
    assertThat(firstVersion.namn).isEqualTo(namn)

    val doesNotExist = loeysingDAO.getLoeysing(id, beforeCreation)
    assertThat(doesNotExist).isNull()
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
    val id1 = createLoeysing(name = namn1, url = "https://www.example1.com")
    val id2 = createLoeysing(name = namn2, url = "https://www.example2.com")

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

  @Test
  @DisplayName(
      "når vi slettar ei løysing, og så opprettar den igjen, så skal den sletta løysinga settast til aktiv")
  fun slettaLoeysingOgOpprettaIgjen() {
    val id = createLoeysing()
    val loeysing = loeysingDAO.getLoeysing(id)!!
    loeysingDAO.delete(id)
    assertThat(loeysingDAO.getLoeysing(id)).isNull()

    val id2 = createLoeysing(loeysing.namn, loeysing.url.toString(), loeysing.orgnummer)
    val loeysing2 = loeysingDAO.getLoeysing(id2)
    assertThat(loeysing2).isNotNull
    assertThat(id2).isEqualTo(id)
  }

  @DisplayName("søk etter løysingar")
  @Nested
  inner class Soek {
    @Test
    @DisplayName("når vi søker etter ei løysing med delar av navnet, så skal vi få treff")
    fun soekEtterNavn() {
      val id = createLoeysing()
      val loeysing = loeysingDAO.getLoeysing(id)!!
      val namn = loeysing.namn
      val searchTerm = namn.dropLast(1)

      val result = loeysingDAO.findLoeysingar(searchTerm)

      assertThat(result).containsExactly(loeysing)
    }

    @Test
    @DisplayName("når vi søker etter ei løysing med delar av orgnummeret, så skal vi få treff")
    fun soekEtterOrgnummer() {
      val id = createLoeysing()
      val loeysing = loeysingDAO.getLoeysing(id)!!
      val orgnummer = loeysing.orgnummer
      val searchTerm = orgnummer.dropLast(1)

      val result = loeysingDAO.findLoeysingar(searchTerm)

      assertThat(result).contains(loeysing)
    }

    @Test
    @DisplayName("når vi søker etter eit namn som ikkje finnes, så skal vi få ei tom liste")
    fun soekEtterUkjentNavn() {
      val searchTerm = UUID.randomUUID().toString()

      val result = loeysingDAO.findLoeysingar(searchTerm)

      assertThat(result).isEmpty()
    }
  }

  /** Lager ei ny løysing med ei oppdatering. */
  private fun createLoeysing(
      name: String = UUID.randomUUID().toString(),
      url: String = "https://www.$name.com",
      orgnummer: String = generateOrgnummer()
  ): Int {
    val id =
        loeysingDAO.createLoeysing("to be updated", URI(url).toURL(), orgnummer).also {
          idsToBeDeleted += it
        }
    val loeysing = loeysingDAO.getLoeysing(id)!!
    loeysingDAO.update(loeysing.copy(namn = name))
    return id
  }

  private fun generateOrgnummer(): String {
    val sevenRandomDigits: String = (1000000..9999999).random().toString()
    return "$sevenRandomDigits${checksumDigit(sevenRandomDigits)}"
  }

  private fun checksumDigit(sevenDigits: String): String {
    val weights = listOf(3, 2, 7, 6, 5, 4, 3, 2)
    val sum = sevenDigits.mapIndexed { index, c -> c.toString().toInt() * weights[index] }.sum()
    return when (val remainder = sum % 11) {
      0 -> {
        "0"
      }
      10 -> {
        val sevenRandomDigits: String = (1000000..9999999).random().toString()
        checksumDigit(sevenRandomDigits) // try again
      }
      else -> {
        (11 - remainder).toString()
      }
    }
  }
}
