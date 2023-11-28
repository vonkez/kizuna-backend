package tr.com.alkimkivanccivi

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import org.jetbrains.exposed.sql.Database
import tr.com.alkimkivanccivi.database.Message
import tr.com.alkimkivanccivi.database.MessageService
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Connection(
    val session: DefaultWebSocketSession,
    val uid: String,
    val messageService: MessageService,
    val connections: ConcurrentHashMap<String, Connection>
) {
    suspend fun startListening() {
        this@Connection.session.apply {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    when {
                        text.startsWith("MSG") -> message(text)
                        text.startsWith("ECHO") -> echo(text, outgoing)
                        text.startsWith("BYE") -> close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }

    suspend fun message(text: String) {
        val (_, targetUid, message) = text.split(" ", limit = 3)

        // Add message to database
        messageService.create(Message(uid, targetUid, message, Date().time))

        // Send message to user if user is online
        if (connections.containsKey(targetUid)) {
            val targetConnection = connections[targetUid]
            targetConnection!!.session.send("MSG $uid $message")
        }else {
            // TODO: send notifcation
        }
    }

    suspend fun echo(text: String, outgoing: SendChannel<Frame>) {
        outgoing.send(Frame.Text("YOU SAID: ${text.split(" ")[1]}"))
    }

}