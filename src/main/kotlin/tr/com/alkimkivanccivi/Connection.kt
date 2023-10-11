package tr.com.alkimkivanccivi

import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession, val database: Database) {
    companion object {
        val lastId = AtomicInteger(0)
    }

    suspend fun startListening(){
        this@Connection.session.apply {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }

    val name = "user${lastId.getAndIncrement()}"
}