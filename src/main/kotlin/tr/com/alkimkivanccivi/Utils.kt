package tr.com.alkimkivanccivi

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.lang.IllegalArgumentException

var a = 0
fun auth(token: String): String? {
    // return a++.toString()
    try {
        val decodedToken = FirebaseAuth.getInstance().verifyIdToken(token)
        return decodedToken.uid
    } catch (e: FirebaseAuthException) {
        return null
    } catch (e: IllegalArgumentException) {
        return null
    }
}