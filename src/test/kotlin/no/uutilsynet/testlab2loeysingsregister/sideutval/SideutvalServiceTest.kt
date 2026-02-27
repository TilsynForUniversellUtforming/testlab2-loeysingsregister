package no.uutilsynet.testlab2loeysingsregister.sideutval

import java.net.URI
import java.util.*
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingEntity
import no.uutilsynet.testlab2loeysingsregister.loeysing.LoeysingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    classes = [SideutvalService::class],
    properties =
        ["spring.datasource.url= jdbc:tc:postgresql:16-alpine:///SideutvalServiceTestTest-db"])
class SideutvalServiceTest() {

  @Autowired lateinit var sideutvalService: SideutvalService

  @MockitoBean lateinit var sideutvalRepository: SideutvalRepository
  @MockitoBean lateinit var loeysingRepository: LoeysingRepository

  @Test
  fun `getOrCreateSideutval returns list of SideUtvalResponse when loeysing exists and sideutval exists`() {
    val sideutvalLookupRequest =
        SideutvalLookupRequest("http://test.no", listOf("address1", "address2"))
    val loeysing = mock(LoeysingEntity::class.java)
    val sideutval = Sideutval(null, loeysing, Sidetype.NETTSIDE, "address1", 1)
    val sideUtvalResponse = SideUtvalResponse(sideutval.id, sideutval.side)

    `when`(loeysing.id).thenReturn(1)

    `when`(loeysingRepository.findByUrl(URI("http://test.no").toURL()))
        .thenReturn(listOf(loeysing) as MutableList<LoeysingEntity>?)
    `when`(sideutvalRepository.findBySideAndLoeysingId("address1", 1)).thenReturn(sideutval)

    sideutvalService.getOrCreateSideutval(sideutvalLookupRequest).onSuccess { responseList ->
      assertEquals(1, responseList.size)
      assertEquals(sideUtvalResponse, responseList[0])
    }
  }

  @Test
  fun `getOrCreateSideutval creates sideutval when not found`() {
    val sideutvalLookupRequest =
        SideutvalLookupRequest("http://test.no", listOf("address1", "address2"))
    val loeysing = mock(LoeysingEntity::class.java)
    val sideutval = Sideutval(null, loeysing, Sidetype.NETTSIDE, "address1", 1)
    val sideUtvalResponse = SideUtvalResponse(sideutval.id, sideutval.side)
    `when`(loeysing.id).thenReturn(1)

    `when`(loeysingRepository.findByUrl(URI("http://test.no").toURL()))
        .thenReturn(listOf(loeysing) as MutableList<LoeysingEntity>?)
    `when`(loeysingRepository.findById(1)).thenReturn(Optional.of(loeysing))
    `when`(sideutvalRepository.findBySideAndLoeysingId("address1", 1)).thenReturn(null)
    `when`(sideutvalRepository.save(any())).thenReturn(sideutval)

    val responseList = sideutvalService.getOrCreateSideutval(sideutvalLookupRequest).getOrThrow()

    assertEquals(2, responseList.size)
    assertEquals(sideUtvalResponse, responseList[0])
    verify(sideutvalRepository, times(2)).save(any(Sideutval::class.java))
  }
}
