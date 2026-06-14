// Caminho: app/src/main/java/com/temaerovendas/ui/screens/list/AircraftListViewModel.kt
package com.temaerovendas.ui.screens.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestoreException
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.annotation.Keep

@Keep
data class AircraftListUiState(
    val isLoading: Boolean = true,
    val aircraft: List<Aircraft> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: AircraftCategory? = null,
    val isAdmin: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class AircraftListViewModel @Inject constructor(
    private val getAllAircraftUseCase: GetAllAircraftUseCase,
    private val searchAircraftUseCase: SearchAircraftUseCase,
    private val filterByCategoryUseCase: FilterAircraftByCategoryUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AircraftListUiState())
    val uiState: StateFlow<AircraftListUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<AircraftCategory?>(null)

    init {
        observeAircraft()
    }

    private fun observeAircraft() {
        viewModelScope.launch {
            try {
                combine(
                    searchQuery.debounce(300),
                    selectedCategory
                ) { query, category -> Pair(query, category) }
                    .flatMapLatest { (query, category) ->
                        when {
                            query.isNotBlank() -> searchAircraftUseCase(query)
                            category != null -> filterByCategoryUseCase(category)
                            else -> getAllAircraftUseCase()
                        }
                    }
                    .catch { e ->
                        // Captura erros emitidos pela stream do Flow
                        Log.e("TEMAerovendas", "Erro no Flow do Firestore: ${e.message}")
                        val msg = if (e is FirebaseFirestoreException) {
                            "Sem permissão para carregar os anúncios. Verifique sua conexão ou login."
                        } else {
                            "Não foi possível carregar a lista de anúncios."
                        }
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    }
                    .collect { list ->
                        _uiState.update { it.copy(isLoading = false, aircraft = list) }
                    }
            } catch (e: FirebaseFirestoreException) {
                // Captura falhas síncronas de permissão (Erro 10 / App Check)
                Log.e("TEMAerovendas", "Erro de permissão no Firestore (Lista): ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Acesso negado aos dados. Verifique seu login."
                    )
                }
            } catch (e: Exception) {
                // Previne qualquer outro crash que mataria o app
                Log.e("TEMAerovendas", "Erro inesperado ao observar anúncios: ${e.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Ocorreu um erro inesperado ao carregar a lista."
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: AircraftCategory?) {
        selectedCategory.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleFavorite(aircraftId: String) {
        val userId = getCurrentUserUseCase()?.uid ?: return
        val aircraft = _uiState.value.aircraft.find { it.id == aircraftId } ?: return

        viewModelScope.launch {
            try {
                if (aircraft.isFavorite) {
                    removeFavoriteUseCase(userId, aircraftId)
                } else {
                    addFavoriteUseCase(userId, aircraftId)
                }
                // Atualiza estado local otimistamente
                _uiState.update { state ->
                    state.copy(
                        aircraft = state.aircraft.map { a ->
                            if (a.id == aircraftId) a.copy(isFavorite = !a.isFavorite) else a
                        }
                    )
                }
            } catch (e: FirebaseFirestoreException) {
                Log.e("TEMAerovendas", "Erro de permissão ao atualizar favorito: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Acesso negado ao favoritar.") }
            } catch (e: Exception) {
                Log.e("TEMAerovendas", "Erro inesperado ao favoritar: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Erro ao atualizar os favoritos.") }
            }
        }
    }
}