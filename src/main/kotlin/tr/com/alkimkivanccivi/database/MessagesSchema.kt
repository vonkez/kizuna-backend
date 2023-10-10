package tr.com.alkimkivanccivi.database

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import tr.com.alkimkivanccivi.plugins.ExposedUser
import tr.com.alkimkivanccivi.plugins.UserService
import java.util.Date


object Messages : IntIdTable() {
    val senderId = integer("sender_id")
    val receiverId = integer("receiver")
    val messageContent = text("message_content")
    val createdAt = long("created_at")
}

@Serializable
data class Message(val senderId: Int, val receiverId: Int, val messageContent: String, val createdAt: Long)

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
            it[createdAt] = message.createdAt
        }[Messages.id].value
    }

    suspend fun read(id: Int): Message? {
        return dbQuery {
            Messages.select { Messages.id eq id }
                .map {
                    Message(
                        it[Messages.senderId],
                        it[Messages.receiverId],
                        it[Messages.messageContent],
                        it[Messages.createdAt]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(id: Int, message: Message) {
        dbQuery {
            Messages.update({ Messages.id eq id }) {
                it[messageContent] = message.messageContent
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Messages.deleteWhere { Messages.id.eq(id) }
        }
    }

    suspend fun readByUsers(senderId: Int, receiverId: Int): List<Message> {
        return dbQuery {
            Messages.select { (Messages.senderId eq senderId) and (Messages.receiverId eq receiverId) }
                .map {
                    Message(
                        it[Messages.senderId],
                        it[Messages.receiverId],
                        it[Messages.messageContent],
                        it[Messages.createdAt]
                    )
                }
        }
    }

    suspend fun readNewMessagesByUsers(senderId: Int, receiverId: Int, timestamp: Long): List<Message> {
        return dbQuery {
            Messages.select { (Messages.createdAt greater timestamp) and (Messages.senderId eq senderId) and (Messages.receiverId eq receiverId) }
                .map {
                    Message(
                        it[Messages.senderId],
                        it[Messages.receiverId],
                        it[Messages.messageContent],
                        it[Messages.createdAt]
                    )
                }
        }
    }

}