package com.example.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.AccountResponse
import com.example.network.ApiService
import com.example.network.CATEGORY_TYPE_EXPENSE
import com.example.network.CATEGORY_TYPE_INCOME
import com.example.network.CategoryResponse
import com.example.network.CreateTransactionRequest
import com.example.network.TXN_TYPE_EXPENSE
import com.example.network.TXN_TYPE_INCOME
import com.example.network.TXN_TYPE_TRANSFER
import com.example.network.TransactionResponse
import com.example.network.toUserMessage
import com.example.ui.common.nowIso8601
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private var initialized = false

    private val _fixedAccountId = MutableStateFlow<Long?>(null)
    val fixedAccountId: StateFlow<Long?> = _fixedAccountId.asStateFlow()

    private val _fixedAccount = MutableStateFlow<AccountResponse?>(null)
    val fixedAccount: StateFlow<AccountResponse?> = _fixedAccount.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initialize(accountId: Long?) {
        if (initialized) return
        initialized = true
        _fixedAccountId.value = accountId
        viewModelScope.launch {
            _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD"
        }
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val allAccounts = apiService.listAccounts().filter { it.isActive }
                _accounts.value = allAccounts
                _categories.value = apiService.listCategories()
                _transactions.value = apiService.listTransactions(_fixedAccountId.value)
                _fixedAccount.value = _fixedAccountId.value?.let { id -> allAccounts.find { it.id == id } }
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load transactions.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Add transaction form ----

    private val _showForm = MutableStateFlow(false)
    val showForm: StateFlow<Boolean> = _showForm.asStateFlow()

    private val _formType = MutableStateFlow(TXN_TYPE_EXPENSE)
    val formType: StateFlow<String> = _formType.asStateFlow()

    private val _formAmount = MutableStateFlow("")
    val formAmount: StateFlow<String> = _formAmount.asStateFlow()

    private val _formFee = MutableStateFlow("")
    val formFee: StateFlow<String> = _formFee.asStateFlow()

    private val _formAccountId = MutableStateFlow<Long?>(null)
    val formAccountId: StateFlow<Long?> = _formAccountId.asStateFlow()

    private val _formSourceAccountId = MutableStateFlow<Long?>(null)
    val formSourceAccountId: StateFlow<Long?> = _formSourceAccountId.asStateFlow()

    private val _formDestinationAccountId = MutableStateFlow<Long?>(null)
    val formDestinationAccountId: StateFlow<Long?> = _formDestinationAccountId.asStateFlow()

    private val _formCategoryId = MutableStateFlow<Long?>(null)
    val formCategoryId: StateFlow<Long?> = _formCategoryId.asStateFlow()

    private val _formDescription = MutableStateFlow("")
    val formDescription: StateFlow<String> = _formDescription.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun openAddForm() {
        val preset = _fixedAccountId.value ?: _accounts.value.firstOrNull()?.id
        _formType.value = TXN_TYPE_EXPENSE
        _formAmount.value = ""
        _formFee.value = ""
        _formAccountId.value = preset
        _formSourceAccountId.value = preset
        _formDestinationAccountId.value = _accounts.value.firstOrNull { it.id != preset }?.id
        _formCategoryId.value = _categories.value.firstOrNull { it.type == CATEGORY_TYPE_EXPENSE }?.id
        _formDescription.value = ""
        _formError.value = null
        _showForm.value = true
    }

    fun dismissForm() {
        _showForm.value = false
    }

    fun onTypeSelected(type: String) {
        _formType.value = type
        _formError.value = null
        val wantedCategoryType = if (type == TXN_TYPE_INCOME) CATEGORY_TYPE_INCOME else CATEGORY_TYPE_EXPENSE
        _formCategoryId.value = _categories.value.firstOrNull { it.type == wantedCategoryType }?.id
    }

    fun onAmountChanged(value: String) {
        _formAmount.value = value
        _formError.value = null
    }

    fun onFeeChanged(value: String) {
        _formFee.value = value
        _formError.value = null
    }

    fun onAccountSelected(id: Long) {
        _formAccountId.value = id
        _formError.value = null
    }

    fun onSourceAccountSelected(id: Long) {
        _formSourceAccountId.value = id
        _formError.value = null
    }

    fun onDestinationAccountSelected(id: Long) {
        _formDestinationAccountId.value = id
        _formError.value = null
    }

    fun onCategorySelected(id: Long) {
        _formCategoryId.value = id
        _formError.value = null
    }

    fun onDescriptionChanged(value: String) {
        _formDescription.value = value
        _formError.value = null
    }

    fun categoriesForFormType(): List<CategoryResponse> {
        val wanted = if (_formType.value == TXN_TYPE_INCOME) CATEGORY_TYPE_INCOME else CATEGORY_TYPE_EXPENSE
        return _categories.value.filter { it.type == wanted }
    }

    fun submitForm() {
        val amount = _formAmount.value.trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formError.value = "Enter a valid amount."
            return
        }
        val fee = _formFee.value.trim().toDoubleOrNull() ?: 0.0
        val type = _formType.value
        val description = _formDescription.value.trim()

        val request = if (type == TXN_TYPE_TRANSFER) {
            val source = _formSourceAccountId.value
            val destination = _formDestinationAccountId.value
            if (source == null || destination == null) {
                _formError.value = "Choose source and destination wallets."
                return
            }
            if (source == destination) {
                _formError.value = "Source and destination must be different wallets."
                return
            }
            CreateTransactionRequest(
                type = type,
                amount = amount,
                fee = fee,
                sourceAccountId = source,
                destinationAccountId = destination,
                description = description,
                date = nowIso8601()
            )
        } else {
            val accountId = _formAccountId.value
            val categoryId = _formCategoryId.value
            if (accountId == null) {
                _formError.value = "Choose a wallet."
                return
            }
            if (categoryId == null) {
                _formError.value = "Choose a category."
                return
            }
            CreateTransactionRequest(
                type = type,
                amount = amount,
                fee = fee,
                accountId = accountId,
                categoryId = categoryId,
                description = description,
                date = nowIso8601()
            )
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                apiService.createTransaction(request)
                _showForm.value = false
                loadAll()
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to save transaction.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
