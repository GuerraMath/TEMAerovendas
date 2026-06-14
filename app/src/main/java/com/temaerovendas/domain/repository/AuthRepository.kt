// Caminho: app/src/main/java/com/temaerovendas/domain/repository/AuthRepository.kt
package com.temaerovendas.domain.repository

import com.temaerovendas.domain.model.AuthState
import com.temaerovendas.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<AuthState>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signUpWithEmail(displayName: String, email: String, password: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun signOut()
    fun getCurrentUser(): User?
    fun isUserLoggedIn(): Boolean

    /**
     * Verifica se o usuário possui papel de administrador, consultando
     * o documento user_roles/{uid}.isAdmin no Firestore.
     */
    suspend fun checkUserRole(uid: String): Boolean
}
