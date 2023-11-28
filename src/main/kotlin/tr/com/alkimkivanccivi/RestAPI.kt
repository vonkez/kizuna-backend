package tr.com.alkimkivanccivi

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import tr.com.alkimkivanccivi.database.MessageService
import java.lang.IllegalArgumentException


fun Application.addRestRoutes(messageService: MessageService) {

    routing {
        get("/readNewMessagesByUser/{timestamp}") {
            val timestamp = call.parameters["timestamp"]?.toLongOrNull()
            if (timestamp == null){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val token = call.request.header("Authorization")

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
            call.respond(messages)

        }
    }
}