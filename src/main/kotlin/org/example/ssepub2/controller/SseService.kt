package org.example.ssepub2.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.coroutines.CoroutineContext

@Service
class SseService {
    private val clients = ConcurrentSkipListMap<String, ServerSentEventCoroutineScope>()

    suspend fun connect(
        merchantNo: String,
        deviceId: String,
        committedMessageId: String? = null,
    ): Flow<ServerSentEvent<String>> {
        val flow = MutableSharedFlow<ServerSentEvent<String>>(replay = 0, extraBufferCapacity = 10)
        val scope = ServerSentEventCoroutineScope(Dispatchers.Default + SupervisorJob(), flow)

        sendMessage(merchantNo, "close")
        clients.remove(merchantNo)?.mutableSharedFlow?.onCompletion {
            println("Client disconnected: $merchantNo")
        }

        clients[merchantNo] = scope

        return flow {
            scope.launch {
                    while (isActive) {
                        val heartbeat = ServerSentEvent.builder<String>()
                            .id(UUID.randomUUID().toString())
                            .event("heartbeat")
                            .data("Heartbeat")
                            .build()
                        flow.emit(heartbeat)
                        delay(3000)
                    }
            }
            scope.mutableSharedFlow.collect { event ->
                emit(event)
            }
        }.takeWhile {
            (
                it.event() == "close" ||
                    it.data() == "close"
                ).not()
        }
    }

    suspend fun sendMessage(clientId: String, message: String?) {
        val broadcastMessage = ServerSentEvent.builder<String>()
            .id(UUID.randomUUID().toString())
            .event("message")
            .data(message ?: "")
            .comment(clientId)
            .build()

        coroutineScope {
            launch {
                clients.forEach { (id, client) ->
                    try {
                        client.mutableSharedFlow.emit(broadcastMessage)
                    } catch (ex: Exception) {
                        println("Failed to send message to client: $id", ex)
                    }
                }
            }
        }
    }

    suspend fun count(): Int = clients.size

    private fun println(s: String, ex: Exception) {
        println(s)
        ex.printStackTrace()
    }
}

class ServerSentEventCoroutineScope(
    override val coroutineContext: CoroutineContext,
    val mutableSharedFlow: MutableSharedFlow<ServerSentEvent<String>>,
) : CoroutineScope
