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


fun Application.addRestRoutes(database: Database) {

    val messageService = MessageService(database)


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

            try {
                val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
                val id = decodedToken.uid
                val messages = messageService.readNewMessagesByUser(id.toInt(),timestamp)
                call.respond(messages)
            } catch (e: FirebaseAuthException){
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }catch (e: IllegalArgumentException){
                call.respond(HttpStatusCode.InternalServerError)
                return@get
            }
        }
    }
}