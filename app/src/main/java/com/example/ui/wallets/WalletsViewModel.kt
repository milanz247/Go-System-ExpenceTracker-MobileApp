package com.example.ui.wallets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.ACCOUNT_TYPES
import com.example.network.ACCOUNT_TYPE_CREDIT_CARD
import com.example.network.AccountResponse
import com.example.network.ApiService
import com.example.network.CreateAccountRequest
import com.example.network.UpdateAccountRequest
import com.example.network.requireSuccess
import com.example.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class WalletsViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showForm = MutableStateFlow(false)
    val showForm: StateFlow<Boolean> = _showForm.asStateFlow()

    private val _editingAccountId = MutableStateFlow<Long?>(null)
    val editingAccountId: StateFlow<Long?> = _editingAccountId.asStateFlow()

    private val _formName = MutableStateFlow("")
    val formName: StateFlow<String> = _formName.asStateFlow()

    private val _formType = MutableStateFlow(ACCOUNT_TYPES.first())
    val formType: StateFlow<String> = _formType.asStateFlow()

    private val _formInitialBalance = MutableStateFlow("")
    val formInitialBalance: StateFlow<String> = _formInitialBalance.asStateFlow()

    private val _formCreditLimit = MutableStateFlow("")
    val formCreditLimit: StateFlow<String> = _formCreditLimit.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _accountPendingArchive = MutableStateFlow<AccountResponse?>(null)
    val accountPendingArchive: StateFlow<AccountResponse?> = _accountPendingArchive.asStateFlow()

    init {
        viewModelScope.launch {
            _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD"
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _accounts.value = apiService.listAccounts().filter { it.isActive }
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load wallets.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    val isEditing: Boolean get() = _editingAccountId.value != null

    fun openAddForm() {
        _editingAccountId.value = null
        _formName.value = ""
        _formType.value = ACCOUNT_TYPES.first()
        _formInitialBalance.value = "0"
        _formCreditLimit.value = ""
        _formError.value = null
        _showForm.value = true
    }

    fun openEditForm(account: AccountResponse) {
        _editingAccountId.value = account.id
        _formName.value = account.name
        _formType.value = account.type
        _formInitialBalance.value = ""
        _formCreditLimit.value = ""
        _formError.value = null
        _showForm.value = true
    }

    fun dismissForm() {
        _showForm.value = false
    }

    fun onNameChanged(value: String) {
        _formName.value = value
        _formError.value = null
    }

    fun onTypeSelected(value: String) {
        _formType.value = value
        _formError.value = null
    }

    fun onInitialBalanceChanged(value: String) {
        _formInitialBalance.value = value
        _formError.value = null
    }

    fun onCreditLimitChanged(value: String) {
        _formCreditLimit.value = value
        _formError.value = null
    }

    fun submitForm() {
        val name = _formName.value.trim()
        if (name.isBlank()) {
            _formError.value = "Name cannot be empty."
            return
        }
        val type = _formType.value
        val creatingNew = _editingAccountId.value == null

        var initialBalance = 0.0
        var creditLimit: Double? = null

        if (creatingNew) {
            initialBalance = _formInitialBalance.value.trim().toDoubleOrNull() ?: run {
                _formError.value = "Enter a valid starting balance."
                return
            }
            if (type == ACCOUNT_TYPE_CREDIT_CARD) {
                creditLimit = _formCreditLimit.value.trim().toDoubleOrNull()
                if (creditLimit == null || creditLimit <= 0) {
                    _formError.value = "Enter a valid credit limit for a credit card wallet."
                    return
                }
            } else if (initialBalance < 0) {
                _formError.value = "Starting balance can't be negative for this wallet type."
                return
            }
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                if (creatingNew) {
                    apiService.createAccount(CreateAccountRequest(name, type, initialBalance, creditLimit))
                } else {
                    apiService.updateAccount(_editingAccountId.value!!, UpdateAccountRequest(name, type))
                }
                _showForm.value = false
                load()
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to save wallet.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun requestArchive(account: AccountResponse) {
        _accountPendingArchive.value = account
    }

    fun cancelArchive() {
        _accountPendingArchive.value = null
    }

    fun confirmArchive() {
        val account = _accountPendingArchive.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                apiService.archiveAccount(account.id).requireSuccess()
                _accountPendingArchive.value = null
                load()
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to archive wallet.")
                _accountPendingArchive.value = null
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
