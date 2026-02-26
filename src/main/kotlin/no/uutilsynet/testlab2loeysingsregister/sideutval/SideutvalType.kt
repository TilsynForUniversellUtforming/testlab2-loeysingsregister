package no.uutilsynet.testlab2loeysingsregister.sideutval

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class SideutvalType(
    @Column(nullable = false, unique = true) val type: String,
    @Column val predefinert: Boolean = false,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int
)
