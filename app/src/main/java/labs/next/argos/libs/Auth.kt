package labs.next.argos.libs

import com.google.firebase.auth.FirebaseAuth

class Auth {
    companion object {
        fun signIn(user: String) {
            val instance: FirebaseAuth = FirebaseAuth.getInstance()

            instance.signInAnonymously()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val authID = instance.currentUser?.uid
                        if (authID != null) Database(user).saveAuthID(authID)
                    }
                }
        }
    }
}