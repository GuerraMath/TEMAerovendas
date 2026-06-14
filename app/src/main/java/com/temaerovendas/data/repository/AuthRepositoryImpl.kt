// Caminho: app/src/main/java/com/temaerovendas/data/repository/AuthRepositoryImpl.kt
package com.temaerovendas.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.temaerovendas.domain.model.AuthState
import com.temaerovendas.domain.model.User
import com.temaerovendas.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val googleSignInClient: GoogleSignInClient
) : AuthRepository {

    override val authState: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)

        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                trySend(AuthState.Unauthenticated)
            } else {
                trySend(AuthState.Authenticated(firebaseUser.toDomainUser()))
            }
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Usuário não encontrado após autenticação"))

            val user = firebaseUser.toDomainUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(
        displayName: String,
        email: String,
        password: String
    ): Result<User> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Não foi possível criar o usuário"))

            if (displayName.isNotBlank()) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()
            }

            val user = firebaseUser.toDomainUser().copy(displayName = displayName)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthError(e)))
        }
    }

    override suspend fun checkUserRole(uid: String): Boolean {
        return try {
            val document = firestore.collection("user_roles").document(uid).get().await()
            // Retorna o valor de isAdmin, se não existir, o padrão é false
            document.getBoolean("isAdmin") ?: false
        } catch (e: Exception) {
            false
        }
    }
    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Usuário não encontrado após autenticação"))

            val user = firebaseUser.toDomainUser()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthError(e)))
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        try {
            googleSignInClient.signOut().await()
        } catch (_: Exception) {
            // Sessão do Google já estava limpa ou Play Services indisponível;
            // o logout do Firebase acima já é suficiente para deslogar o app.
        }
    }

    override fun getCurrentUser(): User? = firebaseAuth.currentUser?.toDomainUser()

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    private fun FirebaseUser.toDomainUser(): User = User(
        uid = uid,
        displayName = displayName ?: "",
        email = email ?: "",
        photoUrl = photoUrl?.toString() ?: "",
        isVerified = isEmailVerified
    )

    private fun mapAuthError(e: Exception): String {
        val code = (e as? com.google.firebase.auth.FirebaseAuthException)?.errorCode
        return when (code) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está cadastrado. Tente entrar."
            "ERROR_WEAK_PASSWORD" -> "A senha deve ter no mínimo 6 caracteres."
            "ERROR_INVALID_EMAIL" -> "E-mail inválido."
            "ERROR_USER_NOT_FOUND", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
                "E-mail ou senha incorretos."
            else -> e.message ?: "Erro de autenticação. Tente novamente."
        }
    }
}
