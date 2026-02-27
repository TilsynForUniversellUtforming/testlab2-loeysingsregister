package no.uutilsynet.testlab2loeysingsregister.sideutval

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("v1/sideutval")
class SideutvalResource(val sideutvalService: SideutvalService) {

  @PostMapping("/getOrCreate")
  fun getSideutval(@RequestBody request: SideutvalLookupRequest): SideutvalLookupResponse {
    return sideutvalService.getSideutval(request)
  }
}
