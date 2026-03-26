package no.uutilsynet.testlab2loeysingsregister.sideutval

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.ManyToOne
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingEntity

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "sidetype", discriminatorType = DiscriminatorType.STRING)
open class Sideutval(
    @ManyToOne(fetch = FetchType.LAZY) open var type: SideutvalType?,
    @ManyToOne(fetch = FetchType.LAZY) open var loeysing: LoeysingEntity,
    @Column(insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    open var sidetype: Sidetype,
    open var side: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) open var id: Int,
)
