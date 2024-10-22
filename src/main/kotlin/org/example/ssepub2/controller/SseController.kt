package org.example.ssepub2.controller

import kotlinx.coroutines.flow.Flow
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SseController(
    private val sseService: SseService,
    private val list: MutableList<SseMessage> = mutableListOf()
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
    suspend fun test() {
        sseService.sendMessage("test", "test")
    }

    @GetMapping("/calc")
    suspend fun calc() {
        sseService.calc()
    }

    @GetMapping("callback")
    suspend fun callback() {
        sseService.callback()
    }

    @PostMapping("callback")
    suspend fun callback(@RequestBody sseMessage: SseMessage) {
        list.add(sseMessage)
    }

    @GetMapping("result")
    suspend fun result(): Result {
        return Result(list)
    }
}

data class Result(
    val result: List<SseMessage>,
    val min: Long?,
    val max: Long?,
    val avg: Double?
) {
    constructor(result: List<SseMessage>) : this(
        result = result,
        min = result.mapNotNull { it.min }.minOrNull(),
        max = result.mapNotNull { it.max }.maxOrNull(),
        avg = result.mapNotNull { it.avg }.average().takeIf { it.isFinite() }
    )
}
data class SseMessage(
    val min: Long?,
    val max: Long?,
    val avg: Double?
)
