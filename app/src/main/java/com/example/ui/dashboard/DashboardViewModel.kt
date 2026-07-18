package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.network.GeminiClient
import com.example.network.GeminiContent
import com.example.network.GeminiGenerationConfig
import com.example.network.GeminiPart
import com.example.network.GeminiRequest
import com.example.network.GeminiThinkingConfig
import com.example.network.Transaction
import com.example.network.Wallet
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    val wallets: StateFlow<List<Wallet>> = _wallets.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Gemini AI Advisor States
    private val _aiQuery = MutableStateFlow("")
    val aiQuery: StateFlow<String> = _aiQuery.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<DashboardEvent>()
    val navigationEvents: SharedFlow<DashboardEvent> = _navigationEvents.asSharedFlow()

    sealed interface DashboardEvent {
        object NavigateToLogin : DashboardEvent
    }

    init {
        loadUserProfile()
        loadDashboardData()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val name = dataStoreManager.nameFlow.firstOrNull() ?: "User"
            val email = dataStoreManager.emailFlow.firstOrNull() ?: ""
            val currency = dataStoreManager.currencyFlow.firstOrNull() ?: "USD"
            _userName.value = name
            _userEmail.value = email
            _userCurrency.value = currency
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val data = apiService.getDashboardData()
                _wallets.value = data.wallets
                _transactions.value = data.transactions
            } catch (e: Exception) {
                _errorMessage.value = "Failed to synchronize ledger: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onAiQueryChanged(value: String) {
        _aiQuery.value = value
    }

    fun askAiAdvisor() {
        val prompt = _aiQuery.value.trim()
        if (prompt.isBlank()) return

        _isAiThinking.value = true
        _aiResponse.value = null
        _aiError.value = null

        viewModelScope.launch {
            try {
                // Fetch key securely via BuildConfig
                val apiKey = BuildConfig.GEMINI_API_KEY

                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    _aiError.value = "API key not configured. Please add your GEMINI_API_KEY inside the Secrets panel of AI Studio."
                    return@launch
                }

                // Prepare the financial context injection for the prompt
                val walletContext = _wallets.value.joinToString { "${it.name} (${it.balance} ${it.currency})" }
                val transactionContext = _transactions.value.joinToString { "${it.title}: ${it.amount} (${it.type})" }

                val fullPrompt = """
                    You are analyzing the financial ledger for user "${_userName.value.ifBlank { "Client" }}".
                    Reporting Currency: ${_userCurrency.value}
                    Active Wallets: $walletContext
                    Recent Transactions: $transactionContext
                    
                    User Query: $prompt
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        thinkingConfig = GeminiThinkingConfig(thinkingLevel = "HIGH"),
                        temperature = 0.7f
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                text = "You are an elite, minimal, and highly professional financial strategist assistant. Your analysis is precise, mathematically thorough, and written in a crisp, clear style. Speak with confidence and objective technical clarity. Focus heavily on optimizing margins, reducing wellness/subscriptions bloat, and building strategic investments."
                            )
                        )
                    )
                )

                val response = GeminiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (text != null) {
                    _aiResponse.value = text
                    _aiQuery.value = "" // clear query on success
                } else {
                    _aiError.value = "No response generated from the thinking core."
                }
            } catch (e: Exception) {
                _aiError.value = "Thinking operation failed: ${e.localizedMessage}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearAuthData()
            _navigationEvents.emit(DashboardEvent.NavigateToLogin)
        }
    }
}
