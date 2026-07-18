package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.network.LoginRequest
import com.example.network.RegisterRequest
import com.example.network.toUserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Currencies offered at registration, mirroring the web frontend's SUPPORTED_CURRENCIES list. */
val SUPPORTED_CURRENCIES = listOf("LKR", "USD", "EUR")

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

class AuthViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _currency = MutableStateFlow(SUPPORTED_CURRENCIES.first())
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<AuthEvent>()
    val navigationEvents: SharedFlow<AuthEvent> = _navigationEvents.asSharedFlow()

    sealed interface AuthEvent {
        object NavigateToDashboard : AuthEvent
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

    fun onCurrencySelected(value: String) {
        _currency.value = value
        _errorMessage.value = null
    }

    fun toggleMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _errorMessage.value = null
        _statusMessage.value = null
    }

    private fun validate(): String? {
        val currentEmail = _email.value.trim()
        val currentPassword = _password.value

        if (currentEmail.isBlank() || currentPassword.isBlank()) {
            return "Email and password cannot be empty."
        }
        if (!EMAIL_REGEX.matches(currentEmail)) {
            return "Enter a valid email address."
        }
        if (_isSignUpMode.value) {
            if (_name.value.trim().isBlank()) {
                return "Name cannot be empty."
            }
            if (currentPassword.length < 8) {
                return "Password must be at least 8 characters."
            }
            if (_currency.value !in SUPPORTED_CURRENCIES) {
                return "Select a reporting currency."
            }
        }
        return null
    }

    fun onSubmit() {
        val validationError = validate()
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }

        val currentEmail = _email.value.trim()
        val currentPassword = _password.value

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                if (_isSignUpMode.value) {
                    apiService.register(
                        RegisterRequest(
                            name = _name.value.trim(),
                            email = currentEmail,
                            password = currentPassword,
                            currency = _currency.value
                        )
                    )
                    // Register succeeds without a token — chain straight into login.
                    _statusMessage.value = "Account created — signing you in…"
                }

                val loginResponse = apiService.login(
                    LoginRequest(email = currentEmail, password = currentPassword)
                )

                dataStoreManager.saveAuthData(
                    token = loginResponse.token,
                    name = loginResponse.user.name,
                    email = currentEmail,
                    currency = loginResponse.user.currency
                )

                _navigationEvents.emit(AuthEvent.NavigateToDashboard)
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Authentication failed. Please try again.")
            } finally {
                _isLoading.value = false
                _statusMessage.value = null
            }
        }
    }
}
