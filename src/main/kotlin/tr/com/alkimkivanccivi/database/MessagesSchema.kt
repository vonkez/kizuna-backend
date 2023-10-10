package tr.com.alkimkivanccivi.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Date


object Messages : IntIdTable() {
    val senderId = integer("sender_id")
    val receiverId = integer("receiver")
    val messageContent = text("message_content")
    val createdAt = long("created_at")
}

data class Message(val senderId: Int, val receiverId: Int, val messageContent: String, val createdAt: Date)

/*
class Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Message>(Messages)
    var senderId by Messages.senderId
    var receiverId     by Messages.receiverId
    var message by Messages.message
}

 */

class MessageService(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Messages)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(message: Message): Int = dbQuery {
        Messages.insert {
            it[senderId] = message.senderId
            it[receiverId] = message.receiverId
            it[messageContent] = message.messageContent
            it[createdAt] = message.createdAt.time
        }[Messages.id].value
    }
}