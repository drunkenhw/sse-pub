package org.example.ssepub2.controller

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.delay
import org.springframework.http.codec.ServerSentEvent

@RestController
class SseController(
    private val sseService: SseService
) {

    @GetMapping("/event", produces = ["text/event-stream"])
    suspend fun sse(@RequestParam merchantNo: String, @RequestParam deviceId: String): Flow<ServerSentEvent<String>> {
        return sseService.connect(merchantNo, deviceId)
    }

    @GetMapping("/count")
    suspend fun count(): Int {
        return sseService.count()
    }

    @GetMapping("/test")
    suspend fun test(){
        sseService.sendMessage("test", "test")
    }
}
