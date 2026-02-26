package no.uutilsynet.testlab2loeysingsregister.loeysing

import java.net.URL
import org.springframework.data.jpa.repository.JpaRepository

interface LoeysingRepository : JpaRepository<LoeysingEntity, Long> {

  fun findByUrl(url: URL): MutableList<LoeysingEntity>
}
