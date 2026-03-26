package no.uutilsynet.testlab2loeysingsregister.loeysing

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.net.URL
import java.time.OffsetDateTime

@Suppress("LongParameterList")
@Entity(name = "loeysing")
class LoeysingEntity(
    @Column val namn: String,
    @Column val url: URL,
    @Column val orgnummer: String,
    @Column(name = "aktiv") var aktiv: Boolean? = null,
    @Column(name = "original") var original: Int? = null,
    @Column(name = "tidspunkt") var tidspunkt: OffsetDateTime? = null,
    @Column(name = "verksemd_id") var verksemdId: Int? = null,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null
)
