package tr.com.alkimkivanccivi

import com.google.firebase.auth.FirebaseAuth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import tr.com.alkimkivanccivi.database.Message
import tr.com.alkimkivanccivi.database.MessageService


@Serializable
data class NewMessagesResult(val messages: List<Message>, val users: Map<String, String>)

fun Application.addRestRoutes(messageService: MessageService) {

    routing {
        get("/newMessages/{timestamp}") {
            val timestamp = call.parameters["timestamp"]?.toLongOrNull()
            if (timestamp == null){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val token = call.request.header("Authorization")
            println("token: $token")
            if (token.isNullOrBlank()){
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }


            val id = auth(token)
            if (id == null)  {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }
            val messages = messageService.readNewMessagesByUser(id,timestamp)
            val userIds = messages.map { if (it.receiverId == id) it.senderId else it.receiverId  }.toSet()
            val users =userIds.map {
                val userRecord = FirebaseAuth.getInstance().getUser(id)
                if (userRecord.displayName == null){
                    return@map id to userRecord.email!!
                } else {
                    return@map id to userRecord.displayName
                }
            }.toMap()
            val result = NewMessagesResult(messages, users)
            call.respond(result)

        }
    }
}