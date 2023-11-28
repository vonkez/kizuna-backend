package tr.com.alkimkivanccivi

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import tr.com.alkimkivanccivi.database.MessageService
import java.io.FileInputStream


fun main() {
    val PORT = System.getenv("PORT")
    val serviceAccount = FileInputStream("service-account-file.json")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .build()

    FirebaseApp.initializeApp(options)

    embeddedServer(Netty, port = PORT.toInt(), host = "0.0.0.0", module = Application::module)
            .start(wait = true)

}

fun Application.module() {

    val DB_URL = System.getenv("DB_URL")
    val DB_USERNAME = System.getenv("DB_USERNAME")
    val DB_PASSWORD = System.getenv("DB_PASSWORD")

    // val database = Database.connect( "jdbc:sqlite:./data.db", "org.sqlite.JDBC" )
    val database = Database.connect(DB_URL, driver = "org.postgresql.Driver",
        user = DB_USERNAME, password = DB_PASSWORD)

    val messageService = MessageService(database)
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    addRestRoutes(messageService)
    configureSockets(messageService)


    // configureSerialization()
    // configureDatabases()
    // configureRouting()
}
