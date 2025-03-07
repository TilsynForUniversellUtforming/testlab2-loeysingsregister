package no.uutilsynet.testlab2loeysingsregister

import java.net.URI
import java.net.URL
import java.time.Instant

fun validateOrgNummer(s: String?): Result<String> = runCatching {
  requireNotNull(s) { "Organisasjonsnummer kan ikkje vere null" }
  require(s.all { it.isDigit() }) { "Organisasjonsnummer kan berre innehalde siffer" }
  require(s.length == 9) { "Organisasjonsnummer må vere 9 siffer" }

  val orgnummer = s.toCharArray().map { it.toString().toInt() }
  val vekter = listOf(3, 2, 7, 6, 5, 4, 3, 2)
  val sum = orgnummer.take(8).zip(vekter).sumOf { (a, b) -> a * b }
  val rest = sum % 11
  val kontrollsiffer = if (rest == 0) 0 else 11 - rest
  if (kontrollsiffer == orgnummer[8]) {
    s
  } else {
    throw IllegalArgumentException("$s er ikkje eit gyldig organisasjonsnummer")
  }
}

fun validateNamn(s: String?): Result<String> = runCatching {
  require(!(s == null || s == "")) { "mangler navn" }
  s
}

fun validateURL(s: String?): Result<URL> = runCatching {
  require(!s.isNullOrBlank()) { "mangler url" }
  val withProtocol =
      if (s.startsWith("http://") || s.startsWith("https://")) {
        s
      } else {
        "https://$s"
      }
  URI(withProtocol).toURL()
}

fun validateIdList(ids: String): Result<List<Int>> = runCatching {
  if (ids == "") emptyList() else ids.split(",").map { it.toInt() }
}

fun validateInstant(s: String?): Result<Instant> =
    runCatching { Instant.parse(s) }.recoverCatching { throw IllegalArgumentException(it) }
