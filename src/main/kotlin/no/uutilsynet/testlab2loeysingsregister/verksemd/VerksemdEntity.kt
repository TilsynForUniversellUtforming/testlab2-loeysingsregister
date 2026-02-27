package no.uutilsynet.testlab2loeysingsregister.verksemd

import jakarta.persistence.*
import java.time.Instant

@Suppress("LongParameterList")
@Entity(name = "verksemd")
class VerksemdEntity(
    val namn: String,
    val organisasjonsnummer: String,
    val institusjonellSektorKode: String,
    val institusjonellSektorKodeBeskrivelse: String,
    val naeringskode: Int,
    val organisasjonsformKode: String,
    val organisasjonsformBeskrivelse: String,
    val fylke: String,
    val kommunenummer: String,
    val postadresse: String?,
    val talTilsette: Int,
    val forvaltningsnivaa: String?,
    val tenesteromraade: String?,
    val aktiv: Boolean = true,
    val original: Int,
    val tidspunkt: Instant = Instant.now(),
    val underAvviking: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int,
) {
  @Column(name = "institusjonell_sektorkode", nullable = false, length = Integer.MAX_VALUE)
  var institusjonellSektorkode: String? = null

  @Column(
      name = "institusjonell_sektorkode_beskrivelse", nullable = false, length = Integer.MAX_VALUE)
  var institusjonellSektorkodeBeskrivelse: String? = null

  @Column(name = "naeringskode_beskrivelse", nullable = false, length = Integer.MAX_VALUE)
  var naeringskodeBeskrivelse: String? = null

  @Column(name = "fylkesnummer", nullable = false, length = 4) var fylkesnummer: String? = null

  @Column(name = "kommune", nullable = false, length = Integer.MAX_VALUE)
  var kommune: String? = null

  @Column(name = "postnummer", length = 4) var postnummer: String? = null

  @Column(name = "poststad", length = Integer.MAX_VALUE) open var poststad: String? = null
}
