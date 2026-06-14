// Caminho: app/src/main/java/com/temaerovendas/ui/screens/register/AircraftRegisterViewModel.kt
package com.temaerovendas.ui.screens.register

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.temaerovendas.domain.model.Aircraft
import com.temaerovendas.domain.model.AircraftCategory
import com.temaerovendas.domain.usecase.CreateAircraftUseCase
import com.temaerovendas.domain.usecase.GetAircraftByIdUseCase
import com.temaerovendas.domain.usecase.GetCurrentUserUseCase
import com.temaerovendas.domain.usecase.UpdateAircraftUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado do formulário de cadastro/edição de aeronave.
 * Todos os campos são representados como String para facilitar
 * a edição em TextField — a conversão/validação ocorre no submit.
 */
data class AircraftRegisterUiState(
    val registration: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val year: String = "",
    val price: String = "",
    val currency: String = "USD",
    val category: AircraftCategory = AircraftCategory.EXECUTIVE_JET,
    val flightHours: String = "",
    val cycles: String = "",
    val passengers: String = "",
    val engines: String = "",
    val configuration: String = "",
    val highlightsText: String = "", // uma linha por destaque
    val description: String = "",
    val location: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val hangarKept: Boolean = true,
    val avionicsPackage: String = "",

    // Condições de pagamento (opcional)
    val offerInstallments: Boolean = false,
    val downPayment: String = "",
    val installmentCount: String = "",
    val installmentValue: String = "",

    val mainPhotoUri: Uri? = null,
    val galleryUris: List<Uri> = emptyList(),

    // Modo edição — preenchido apenas quando estamos editando um anúncio existente
    val isEditMode: Boolean = false,
    val isLoadingExisting: Boolean = false,
    val existingMainPhotoUrl: String = "",
    val existingGalleryUrls: List<String> = emptyList(),
    val existingOwnerId: String = "",

    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

@HiltViewModel
class AircraftRegisterViewModel @Inject constructor(
    private val createAircraftUseCase: CreateAircraftUseCase,
    private val updateAircraftUseCase: UpdateAircraftUseCase,
    private val getAircraftByIdUseCase: GetAircraftByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AircraftRegisterUiState())
    val uiState: StateFlow<AircraftRegisterUiState> = _uiState.asStateFlow()

    // Id do anúncio em edição — vem da rota de navegação. Nulo = modo cadastro novo.
    private val aircraftId: String? = savedStateHandle.get<String>("aircraftId")

    init {
        if (aircraftId != null) {
            loadExistingAircraft(aircraftId)
        }
    }

    private fun loadExistingAircraft(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingExisting = true, isEditMode = true) }
            val aircraft = getAircraftByIdUseCase(id)
            if (aircraft != null) {
                _uiState.update {
                    it.copy(
                        isLoadingExisting = false,
                        registration = aircraft.registration,
                        model = aircraft.model,
                        manufacturer = aircraft.manufacturer,
                        year = if (aircraft.year > 0) aircraft.year.toString() else "",
                        price = if (aircraft.price > 0) aircraft.price.toString() else "",
                        currency = aircraft.currency,
                        category = aircraft.category,
                        flightHours = aircraft.flightHours.toString(),
                        cycles = if (aircraft.cycles > 0) aircraft.cycles.toString() else "",
                        passengers = if (aircraft.passengers > 0) aircraft.passengers.toString() else "",
                        engines = aircraft.engines,
                        configuration = aircraft.configuration,
                        highlightsText = aircraft.highlights.joinToString("\n"),
                        description = aircraft.description,
                        location = aircraft.location,
                        contactEmail = aircraft.contactEmail,
                        contactPhone = aircraft.contactPhone,
                        hangarKept = aircraft.hangarKept,
                        avionicsPackage = aircraft.avionicsPackage,
                        offerInstallments = aircraft.installmentCount > 0,
                        downPayment = if (aircraft.downPayment > 0) aircraft.downPayment.toString() else "",
                        installmentCount = if (aircraft.installmentCount > 0) aircraft.installmentCount.toString() else "",
                        installmentValue = if (aircraft.installmentValue > 0) aircraft.installmentValue.toString() else "",
                        existingMainPhotoUrl = aircraft.mainPhotoUrl,
                        existingGalleryUrls = aircraft.photoUrls,
                        existingOwnerId = aircraft.ownerId
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoadingExisting = false,
                        errorMessage = "Não foi possível carregar os dados do anúncio."
                    )
                }
            }
        }
    }

    fun onRegistrationChange(value: String) = update { it.copy(registration = value.uppercase()) }
    fun onModelChange(value: String) = update { it.copy(model = value) }
    fun onManufacturerChange(value: String) = update { it.copy(manufacturer = value) }
    fun onYearChange(value: String) = update { it.copy(year = value.filter { c -> c.isDigit() }.take(4)) }
    fun onPriceChange(value: String) = update { it.copy(price = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onCurrencyChange(value: String) = update { it.copy(currency = value) }
    fun onCategoryChange(value: AircraftCategory) = update { it.copy(category = value) }
    fun onFlightHoursChange(value: String) = update { it.copy(flightHours = value.filter { c -> c.isDigit() }) }
    fun onCyclesChange(value: String) = update { it.copy(cycles = value.filter { c -> c.isDigit() }) }
    fun onPassengersChange(value: String) = update { it.copy(passengers = value.filter { c -> c.isDigit() }) }
    fun onEnginesChange(value: String) = update { it.copy(engines = value) }
    fun onConfigurationChange(value: String) = update { it.copy(configuration = value) }
    fun onHighlightsChange(value: String) = update { it.copy(highlightsText = value) }
    fun onDescriptionChange(value: String) = update { it.copy(description = value) }
    fun onLocationChange(value: String) = update { it.copy(location = value) }
    fun onContactEmailChange(value: String) = update { it.copy(contactEmail = value) }
    fun onContactPhoneChange(value: String) = update { it.copy(contactPhone = value) }
    fun onHangarKeptChange(value: Boolean) = update { it.copy(hangarKept = value) }
    fun onAvionicsPackageChange(value: String) = update { it.copy(avionicsPackage = value) }

    fun onOfferInstallmentsChange(value: Boolean) = update {
        if (value) it.copy(offerInstallments = true)
        else it.copy(
            offerInstallments = false,
            downPayment = "",
            installmentCount = "",
            installmentValue = ""
        )
    }
    fun onDownPaymentChange(value: String) = update { it.copy(downPayment = value.filter { c -> c.isDigit() || c == '.' }) }
    fun onInstallmentCountChange(value: String) = update { it.copy(installmentCount = value.filter { c -> c.isDigit() }.take(3)) }
    fun onInstallmentValueChange(value: String) = update { it.copy(installmentValue = value.filter { c -> c.isDigit() || c == '.' }) }

    fun onMainPhotoSelected(uri: Uri?) = update { it.copy(mainPhotoUri = uri) }

    fun onGalleryPhotosSelected(uris: List<Uri>) = update {
        // Limita a 8 fotos de galeria para não sobrecarregar o upload
        it.copy(galleryUris = (it.galleryUris + uris).distinct().take(8))
    }

    fun onRemoveGalleryPhoto(uri: Uri) = update {
        it.copy(galleryUris = it.galleryUris.filterNot { existing -> existing == uri })
    }

    fun onRemoveExistingGalleryPhoto(url: String) = update {
        it.copy(existingGalleryUrls = it.existingGalleryUrls.filterNot { existing -> existing == url })
    }

    private fun update(transform: (AircraftRegisterUiState) -> AircraftRegisterUiState) {
        _uiState.update(transform)
    }

    /**
     * Valida os campos obrigatórios e retorna um mapa de erros.
     * Mapa vazio = formulário válido.
     */
    private fun validate(state: AircraftRegisterUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (state.model.isBlank()) errors["model"] = "Informe o modelo"
        if (state.manufacturer.isBlank()) errors["manufacturer"] = "Informe o fabricante"

        val year = state.year.toIntOrNull()
        if (year == null || year < 1940 || year > 2100) errors["year"] = "Ano inválido"

        val price = state.price.toDoubleOrNull()
        if (price == null || price <= 0.0) errors["price"] = "Preço inválido"

        if (state.flightHours.toIntOrNull() == null) errors["flightHours"] = "Informe as horas de voo"

        if (state.contactEmail.isBlank() && state.contactPhone.isBlank()) {
            errors["contact"] = "Informe ao menos um contato (e-mail ou telefone)"
        }

        if (state.mainPhotoUri == null && state.existingMainPhotoUrl.isBlank()) {
            errors["mainPhoto"] = "Selecione a foto principal"
        }

        if (state.offerInstallments) {
            val down = state.downPayment.toDoubleOrNull()
            val count = state.installmentCount.toIntOrNull()
            val value = state.installmentValue.toDoubleOrNull()

            if (down == null || down < 0.0) errors["downPayment"] = "Valor de entrada inválido"
            if (count == null || count <= 0) errors["installmentCount"] = "Informe o número de parcelas"
            if (value == null || value <= 0.0) errors["installmentValue"] = "Valor da parcela inválido"
        }

        return errors
    }

    fun submit() {
        val current = _uiState.value
        val errors = validate(current)

        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(fieldErrors = errors, errorMessage = null) }
            return
        }

        val currentUid = getCurrentUserUseCase()?.uid
        if (currentUid == null) {
            _uiState.update { it.copy(errorMessage = "Usuário não autenticado. Faça login novamente.") }
            return
        }
        // No modo edição mantém o dono original do anúncio (relevante quando é um admin editando);
        // no cadastro novo, o dono é o próprio usuário logado.
        val ownerId = if (current.isEditMode) current.existingOwnerId.ifBlank { currentUid } else currentUid

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, fieldErrors = emptyMap()) }

            val aircraft = Aircraft(
                registration = current.registration.trim(),
                model = current.model.trim(),
                manufacturer = current.manufacturer.trim(),
                year = current.year.toIntOrNull() ?: 0,
                price = current.price.toDoubleOrNull() ?: 0.0,
                currency = current.currency,
                category = current.category,
                flightHours = current.flightHours.toIntOrNull() ?: 0,
                cycles = current.cycles.toIntOrNull() ?: 0,
                passengers = current.passengers.toIntOrNull() ?: 0,
                engines = current.engines.trim(),
                configuration = current.configuration.trim(),
                highlights = current.highlightsText
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() },
                contactEmail = current.contactEmail.trim(),
                contactPhone = current.contactPhone.trim(),
                description = current.description.trim(),
                hangarKept = current.hangarKept,
                avionicsPackage = current.avionicsPackage.trim(),
                location = current.location.trim(),
                downPayment = if (current.offerInstallments) current.downPayment.toDoubleOrNull() ?: 0.0 else 0.0,
                installmentCount = if (current.offerInstallments) current.installmentCount.toIntOrNull() ?: 0 else 0,
                installmentValue = if (current.offerInstallments) current.installmentValue.toDoubleOrNull() ?: 0.0 else 0.0
            )

            val result = if (current.isEditMode && aircraftId != null) {
                updateAircraftUseCase(
                    aircraftId = aircraftId,
                    aircraft = aircraft.copy(mainPhotoUrl = current.existingMainPhotoUrl),
                    newMainPhotoUri = current.mainPhotoUri,
                    newGalleryUris = current.galleryUris,
                    existingGalleryUrls = current.existingGalleryUrls,
                    ownerId = ownerId
                )
            } else {
                createAircraftUseCase(
                    aircraft = aircraft,
                    mainPhotoUri = current.mainPhotoUri,
                    galleryUris = current.galleryUris,
                    ownerId = ownerId
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "Erro ao cadastrar aeronave. Tente novamente."
                        )
                    }
                }
            )
        }
    }

    fun consumeError() = update { it.copy(errorMessage = null) }
}
