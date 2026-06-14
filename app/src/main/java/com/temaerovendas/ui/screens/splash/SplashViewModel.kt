// Caminho: app/src/main/java/com/temaerovendas/ui/screens/splash/SplashViewModel.kt
package com.temaerovendas.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.temaerovendas.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { Loading, Login, AircraftList }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // Espera mínima para a animação da splash
            delay(2000L)
            _destination.value = if (authRepository.isUserLoggedIn()) {
                SplashDestination.AircraftList
            } else {
                SplashDestination.Login
            }
        }
    }
}
