package no.uutilsynet.testlab2loeysingsregister

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidatorsKtTest {
  @DisplayName("orgnummer")
  @Nested
  inner class Orgnummer {
    @ParameterizedTest
    @ValueSource(strings = ["123456785", "938644500"])
    @DisplayName("når vi validerer eit gyldig orgnummer, så skal vi få success")
    fun gyldigOrgNummer(s: String) {
      val result = validateOrgNummer(s)
      assertThat(result).isEqualTo(Result.success(s))
    }

    @Test
    @DisplayName("når vi validerer eit ugyldig orgnummer, så skal vi få failure")
    fun ugyldigOrgNummer() {
      val orgnummer = "123456789"
      val result = validateOrgNummer(orgnummer)
      assertThat(result.isFailure).isTrue()
    }

    @Test
    @DisplayName("når vi validerer noko anna enn eit orgnummer, så skal vi få failure")
    fun ikkjeEitOrgNummer() {
      val orgnummer = "hello world"
      val result = validateOrgNummer(orgnummer)
      assertThat(result.isFailure).isTrue()
    }

    @Test
    @DisplayName("når vi validerer null, så skal vi få failure")
    fun nullOrgNummer() {
      val orgnummer = null
      val result = validateOrgNummer(orgnummer)
      assertThat(result.isFailure).isTrue()
    }
  }

  @DisplayName("id")
  @Nested
  inner class Id {
    @Test
    fun `ein gyldig id skal returnere success`() {
      val validId = "12345"
      val result = validateId(validId)
      assertEquals(12345, result.getOrNull())
    }

    @Test
    fun `ein ugyldig id skal returnere failure`() {
      val invalidId = "invalidId"
      val result = validateId(invalidId)
      assertTrue(result.isFailure)
    }

    @Test
    fun `null er ein gyldig input`() {
      val nullId: String? = null
      val result = validateId(nullId)
      assertTrue(result.isSuccess)
    }
  }

  @DisplayName("idList")
  @Nested
  inner class IdList {
    @DisplayName("når vi validerer ein tom streng, så skal vi få ei tom liste")
    @Test
    fun emptyString() {
      assertThat(validateIdList("")).isEqualTo(Result.success(emptyList<Int>()))
    }

    @DisplayName("når vi validerer ein streng med eit tal, så skal vi få ei liste med eit tal")
    @Test
    fun oneNumber() {
      assertThat(validateIdList("1")).isEqualTo(Result.success(listOf(1)))
    }

    @DisplayName("når vi validerer ein streng med to tal, så skal vi få ei liste me to tal")
    @ParameterizedTest
    @ValueSource(strings = ["1,2", "1, 2", "     1,     2  "])
    fun twoNumbers() {
      assertThat(validateIdList("1,2")).isEqualTo(Result.success(listOf(1, 2)))
    }

    @DisplayName("når vi validerer ein streng med noko anna en tal, så skal det feile")
    @Test
    fun notNumbers() {
      val result = validateIdList("one,two")
      assertThat(result.isFailure).isTrue()
      assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
    }
  }
}
