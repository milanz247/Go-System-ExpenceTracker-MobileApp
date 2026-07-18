package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.AccountResponse
import com.example.network.ApiService
import com.example.network.SummaryResponse
import com.example.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _summary = MutableStateFlow<SummaryResponse?>(null)
    val summary: StateFlow<SummaryResponse?> = _summary.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _userName.value = dataStoreManager.nameFlow.firstOrNull() ?: ""
            _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD"
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _summary.value = apiService.getSummary()
                _accounts.value = apiService.listAccounts().filter { it.isActive }
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load dashboard data.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
