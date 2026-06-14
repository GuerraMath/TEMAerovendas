// Caminho: app/src/main/java/com/temaerovendas/domain/usecase/AuthUseCases.kt
package com.temaerovendas.domain.usecase

import com.temaerovendas.domain.model.AuthState
import com.temaerovendas.domain.model.User
import com.temaerovendas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}

class SignInWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User> =
        authRepository.signInWithGoogle(idToken)
}

class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(displayName: String, email: String, password: String): Result<User> =
        authRepository.signUpWithEmail(displayName, email, password)
}

class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        authRepository.signInWithEmail(email, password)
}

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() = authRepository.signOut()
}

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): User? = authRepository.getCurrentUser()
}

/**
 * Verifica se o usuário logado possui papel de administrador
 * (documento user_roles/{uid}.isAdmin no Firestore).
 */
class CheckUserRoleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(uid: String): Boolean = authRepository.checkUserRole(uid)
}
