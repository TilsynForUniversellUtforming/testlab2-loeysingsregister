package no.uutilsynet.testlab2loeysingsregister.sideutval

import org.springframework.data.jpa.repository.JpaRepository

interface SideutvalRepository : JpaRepository<Sideutval, Long> {
  fun findSideutvalBySide(side: String): Sideutval?

  fun findBySideAndLoeysingId(side: String, loeysingId: Int): Sideutval?

  fun findByLoeysingId(loeysingId: Int): List<Sideutval>
}
