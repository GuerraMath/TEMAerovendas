// Caminho: app/src/main/java/com/temaerovendas/domain/model/User.kt
package com.temaerovendas.domain.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isVerified: Boolean = false
)

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
