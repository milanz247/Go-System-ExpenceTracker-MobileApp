package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.network.ChangePasswordRequest
import com.example.network.SUPPORTED_CURRENCIES
import com.example.network.SUPPORTED_TIMEZONES
import com.example.network.UpdateProfileRequest
import com.example.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _currency = MutableStateFlow(SUPPORTED_CURRENCIES.first())
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _timezone = MutableStateFlow(SUPPORTED_TIMEZONES.first())
    val timezone: StateFlow<String> = _timezone.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var loaded = false

    fun ensureLoaded() {
        if (loaded) return
        loaded = true
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val profile = apiService.getProfile()
                _name.value = profile.name
                _email.value = profile.email
                _currency.value = profile.currency
                _timezone.value = profile.timezone
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load profile.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onNameChanged(value: String) {
        _name.value = value
        _saveMessage.value = null
    }

    fun onCurrencySelected(value: String) {
        _currency.value = value
        _saveMessage.value = null
    }

    fun onTimezoneSelected(value: String) {
        _timezone.value = value
        _saveMessage.value = null
    }

    fun saveProfile() {
        val trimmedName = _name.value.trim()
        if (trimmedName.isBlank()) {
            _errorMessage.value = "Name cannot be empty."
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            _saveMessage.value = null
            try {
                val updated = apiService.updateProfile(UpdateProfileRequest(trimmedName, _currency.value, _timezone.value))
                _name.value = updated.name
                _currency.value = updated.currency
                _timezone.value = updated.timezone
                dataStoreManager.updateUserInfo(
                    name = updated.name,
                    email = _email.value,
                    currency = updated.currency
                )
                _saveMessage.value = "Profile updated."
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to update profile.")
            } finally {
                _isSaving.value = false
            }
        }
    }

    // ---- Change password ----

    private val _oldPassword = MutableStateFlow("")
    val oldPassword: StateFlow<String> = _oldPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _passwordSuccessMessage = MutableStateFlow<String?>(null)
    val passwordSuccessMessage: StateFlow<String?> = _passwordSuccessMessage.asStateFlow()

    fun onOldPasswordChanged(value: String) {
        _oldPassword.value = value
        _passwordError.value = null
    }

    fun onNewPasswordChanged(value: String) {
        _newPassword.value = value
        _passwordError.value = null
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
        _passwordError.value = null
    }

    fun submitPasswordChange() {
        val oldPw = _oldPassword.value
        val newPw = _newPassword.value
        val confirmPw = _confirmPassword.value
        if (oldPw.isBlank() || newPw.isBlank()) {
            _passwordError.value = "Fill in all password fields."
            return
        }
        if (newPw.length < 8) {
            _passwordError.value = "New password must be at least 8 characters."
            return
        }
        if (newPw == oldPw) {
            _passwordError.value = "New password must differ from the current one."
            return
        }
        if (newPw != confirmPw) {
            _passwordError.value = "New password and confirmation don't match."
            return
        }
        viewModelScope.launch {
            _isSaving.value = true
            _passwordError.value = null
            try {
                apiService.changePassword(ChangePasswordRequest(oldPw, newPw, confirmPw))
                _oldPassword.value = ""
                _newPassword.value = ""
                _confirmPassword.value = ""
                _passwordSuccessMessage.value = "Password updated."
            } catch (e: Exception) {
                _passwordError.value = e.toUserMessage("Failed to change password.")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearPasswordSuccessMessage() {
        _passwordSuccessMessage.value = null
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            dataStoreManager.clearAuthData()
            onLoggedOut()
        }
    }
}
