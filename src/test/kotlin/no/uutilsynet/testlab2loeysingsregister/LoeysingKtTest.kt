package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Loesying test")
class LoeysingKtTest {
  @DisplayName("diff")
  @Nested
  inner class Diff {
    @DisplayName("når vi differ to like løysingar, så skal ingen av felta vere endra")
    @Test
    fun toLike() {
      val loeysing =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      assertThat(diff(loeysing, loeysing)).isEqualTo(LoeysingDiff(null, null, null, null))
    }

    @DisplayName("når vi differ to ulike løysingar, så skal alle felta vere endra")
    @Test
    fun toUlike() {
      val uutilsynet =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      val askerKommune =
          Loeysing(2, "Asker kommune", URI("https://www.asker.kommune.no/").toURL(), "920125298")
      assertThat(diff(uutilsynet, askerKommune))
          .isEqualTo(
              LoeysingDiff(
                  2, "Asker kommune", URI("https://www.asker.kommune.no/").toURL(), "920125298"))
    }

    @DisplayName("når vi differ samme løysing med ulikt namn, så skal namnet vere endra")
    @Test
    fun uliktNamn() {
      val loeysing =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      val loeysing2 = Loeysing(1, "TUU", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      assertThat(diff(loeysing, loeysing2)).isEqualTo(LoeysingDiff(null, "TUU", null, null))
    }

    @DisplayName("når vi differ samme løysing med ulik url, så skal url vere endra")
    @Test
    fun ulikUrl() {
      val loeysing =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      val loeysing2 = Loeysing(1, "UUTilsynet", URI("https://www.digdir.no/").toURL(), "991825827")
      assertThat(diff(loeysing, loeysing2))
          .isEqualTo(LoeysingDiff(null, null, URI("https://www.digdir.no/").toURL(), null))
    }

    @DisplayName("når vi differ samme løysing med ulikt orgnummer, så skal orgnummer vere endra")
    @Test
    fun uliktOrgnummer() {
      val loeysing =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "991825827")
      val loeysing2 =
          Loeysing(1, "UUTilsynet", URI("https://www.uutilsynet.no/").toURL(), "123456785")
      assertThat(diff(loeysing, loeysing2)).isEqualTo(LoeysingDiff(null, null, null, "123456785"))
    }
  }
}
