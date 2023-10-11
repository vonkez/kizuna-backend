package tr.com.alkimkivanccivi

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.Database
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.LinkedHashSet

fun Application.configureSockets(database: Database) {
    val connections = ConcurrentHashMap<String, Connection>()
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws") { // websocketSession
            val frame = incoming.receive()
            val text = (frame as Frame.Text).readText()
            if (text.startsWith("Auth ")){
                val uid = auth(text.split(" ")[1])
                if (uid === null) {
                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR,"authentication failed"))
                }else{
                    val connection = Connection(this, database)
                    connections[uid] = connection
                    connection.startListening()
                }
            } else{
                close(CloseReason(CloseReason.Codes.INTERNAL_ERROR,"authentication failed"))
            }

        }
    }
}
