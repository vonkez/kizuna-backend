package tr.com.alkimkivanccivi.database

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


object Messages : IntIdTable() {
    val senderId = varchar("sender_id", 128)
    val receiverId = varchar("receiver", 128)
    val messageContent = text("message_content")
    val createdAt = long("created_at")
}

@Serializable
data class Message(val senderId: String, val receiverId: String, val messageContent: String, val createdAt: Long)

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

    suspend fun readByUser(userId: String): List<Message> {
        return dbQuery {
            Messages.select { (Messages.senderId eq userId) or (Messages.receiverId eq userId) }
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

    suspend fun readNewMessagesByUser(userId: String, timestamp: Long): List<Message> {
        return dbQuery {
            Messages.select { (Messages.createdAt greater timestamp) and ((Messages.senderId eq userId) or (Messages.receiverId eq userId)) }
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