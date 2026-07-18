package com.example.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.network.LoginRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _currency = MutableStateFlow("USD")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<LoginEvent>()
    val navigationEvents: SharedFlow<LoginEvent> = _navigationEvents.asSharedFlow()

    sealed interface LoginEvent {
        object NavigateToDashboard : LoginEvent
    }

    fun onEmailChanged(value: String) {
        _email.value = value
        _errorMessage.value = null
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        _errorMessage.value = null
    }

    fun onNameChanged(value: String) {
        _name.value = value
        _errorMessage.value = null
    }

    fun onCurrencyChanged(value: String) {
        _currency.value = value
        _errorMessage.value = null
    }

    fun toggleMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _errorMessage.value = null
    }

    fun onSubmit() {
        val currentEmail = _email.value.trim()
        val currentPassword = _password.value
        val currentName = _name.value.trim()
        val currentCurrency = _currency.value.trim()

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            _errorMessage.value = "Email and Password cannot be empty."
            return
        }

        if (_isSignUpMode.value && currentName.isBlank()) {
            _errorMessage.value = "Name cannot be empty in Sign Up mode."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = if (_isSignUpMode.value) {
                    apiService.register(
                        LoginRequest(
                            email = currentEmail,
                            password = currentPassword,
                            name = currentName,
                            currency = currentCurrency
                        )
                    )
                } else {
                    apiService.login(
                        LoginRequest(
                            email = currentEmail,
                            password = currentPassword
                        )
                    )
                }

                // Save authentication data in DataStore securely
                dataStoreManager.saveAuthData(
                    token = response.token,
                    name = response.name,
                    email = response.email,
                    currency = response.currency
                )

                _navigationEvents.emit(LoginEvent.NavigateToDashboard)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "An error occurred during authentication."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
