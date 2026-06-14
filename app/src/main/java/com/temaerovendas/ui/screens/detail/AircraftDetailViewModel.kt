// Caminho: app/src/main/java/com/temaerovendas/ui/screens/detail/AircraftDetailViewModel.kt
package com.temaerovendas.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.ReportReason
import com.temaerovendas.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import androidx.annotation.Keep

@Keep
data class AircraftDetailUiState(
    val isLoading: Boolean = true,
    val aircraft: Aircraft? = null,
    val isAdmin: Boolean = false,
    val isOwner: Boolean = false,
    val errorMessage: String? = null,
    val isSubmittingReport: Boolean = false,
    val reportSubmitted: Boolean = false
)

@HiltViewModel
class AircraftDetailViewModel @Inject constructor(
    private val getAircraftByIdUseCase: GetAircraftByIdUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val submitContactRequestUseCase: SubmitContactRequestUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val checkUserRoleUseCase: CheckUserRoleUseCase,
    private val submitReportUseCase: SubmitReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AircraftDetailUiState())
    val uiState: StateFlow<AircraftDetailUiState> = _uiState.asStateFlow()

    fun loadAircraft(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val aircraft = getAircraftByIdUseCase(id)
            val currentUid = getCurrentUserUseCase()?.uid
            val isOwner = currentUid != null && aircraft != null && currentUid == aircraft.ownerId
            val isAdmin = currentUid?.let { checkUserRoleUseCase(it) } ?: false
            _uiState.update {
                it.copy(
                    isLoading = false,
                    aircraft = aircraft,
                    isOwner = isOwner,
                    isAdmin = isAdmin
                )
            }
        }
    }

    fun toggleFavorite(aircraftId: String) {
        val userId = getCurrentUserUseCase()?.uid ?: return
        val aircraft = _uiState.value.aircraft ?: return
        viewModelScope.launch {
            if (aircraft.isFavorite) {
                removeFavoriteUseCase(userId, aircraftId)
            } else {
                addFavoriteUseCase(userId, aircraftId)
            }
            _uiState.update { it.copy(aircraft = aircraft.copy(isFavorite = !aircraft.isFavorite)) }
        }
    }

    fun submitContactRequest(aircraftId: String, message: String) {
        val userId = getCurrentUserUseCase()?.uid ?: return

        viewModelScope.launch {
            try {
                // Tenta enviar a solicitação para o banco
                submitContactRequestUseCase(aircraftId, userId, message)

                // Aqui você pode opcionalmente atualizar a UI informando sucesso
                // _uiState.update { it.copy(errorMessage = "Solicitação enviada com sucesso!") }

            } catch (e: FirebaseFirestoreException) {
                Log.e("TEMAerovendas", "Erro de permissão ao solicitar contato: ${e.message}")
                _uiState.update {
                    it.copy(errorMessage = "Não foi possível enviar a solicitação. Verifique sua permissão.")
                }
            } catch (e: Exception) {
                Log.e("TEMAerovendas", "Erro inesperado ao solicitar contato: ${e.message}")
                _uiState.update {
                    it.copy(errorMessage = "Ocorreu um erro ao tentar enviar a mensagem.")
                }
            }
        }
    }

    /**
     * Envia a denúncia do anúncio atual (Política de UGC). Disponível para
     * qualquer usuário autenticado, a partir do botão "Denunciar" da ficha técnica.
     */
    fun submitReport(aircraftId: String, reason: ReportReason, details: String) {
        val user = getCurrentUserUseCase() ?: run {
            _uiState.update { it.copy(errorMessage = "Faça login para denunciar este anúncio.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReport = true) }
            val result = submitReportUseCase(aircraftId, user.uid, user.email, reason, details)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmittingReport = false, reportSubmitted = true) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isSubmittingReport = false,
                            errorMessage = "Não foi possível enviar a denúncia. Tente novamente."
                        )
                    }
                }
            )
        }
    }

    fun consumeReportSubmitted() {
        _uiState.update { it.copy(reportSubmitted = false) }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
