package com.example.ui.storetabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.AccountResponse
import com.example.network.ApiService
import com.example.network.CATEGORY_TYPE_EXPENSE
import com.example.network.CategoryResponse
import com.example.network.CreateCreditorRequest
import com.example.network.CreditorResponse
import com.example.network.PurchaseResponse
import com.example.network.RecordPurchaseRequest
import com.example.network.RecordSettlementRequest
import com.example.network.SettlementResponse
import com.example.network.toUserMessage
import com.example.ui.common.nowIso8601
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StoreTabsViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _stores = MutableStateFlow<List<CreditorResponse>>(emptyList())
    val stores: StateFlow<List<CreditorResponse>> = _stores.asStateFlow()

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

    private var listInitialized = false

    fun ensureListLoaded() {
        if (listInitialized) return
        listInitialized = true
        viewModelScope.launch { _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD" }
        loadStores()
    }

    fun loadStores() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stores.value = apiService.listStores().filter { it.isActive }
                _accounts.value = apiService.listAccounts().filter { it.isActive }
                _categories.value = apiService.listCategories().filter { it.type == CATEGORY_TYPE_EXPENSE }
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load store tabs.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Add shop form ----

    private val _showAddShopForm = MutableStateFlow(false)
    val showAddShopForm: StateFlow<Boolean> = _showAddShopForm.asStateFlow()

    private val _formShopName = MutableStateFlow("")
    val formShopName: StateFlow<String> = _formShopName.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun openAddShopForm() {
        _formShopName.value = ""
        _formError.value = null
        _showAddShopForm.value = true
    }

    fun dismissAddShopForm() {
        _showAddShopForm.value = false
    }

    fun onShopNameChanged(value: String) {
        _formShopName.value = value
        _formError.value = null
    }

    fun submitAddShopForm() {
        val name = _formShopName.value.trim()
        if (name.isBlank()) {
            _formError.value = "Enter a shop name."
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                apiService.createStore(CreateCreditorRequest(name))
                _showAddShopForm.value = false
                loadStores()
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to add shop.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // ---- Detail screen (purchases + settlements for one creditor) ----

    private var detailInitializedFor: Long? = null

    private val _detailCreditor = MutableStateFlow<CreditorResponse?>(null)
    val detailCreditor: StateFlow<CreditorResponse?> = _detailCreditor.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseResponse>>(emptyList())
    val purchases: StateFlow<List<PurchaseResponse>> = _purchases.asStateFlow()

    private val _settlements = MutableStateFlow<List<SettlementResponse>>(emptyList())
    val settlements: StateFlow<List<SettlementResponse>> = _settlements.asStateFlow()

    fun ensureDetailLoaded(creditorId: Long) {
        if (detailInitializedFor == creditorId) return
        detailInitializedFor = creditorId
        viewModelScope.launch { _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD" }
        loadDetail(creditorId)
    }

    fun loadDetail(creditorId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _stores.value = apiService.listStores()
                _detailCreditor.value = _stores.value.find { it.id == creditorId }
                _accounts.value = apiService.listAccounts().filter { it.isActive }
                _categories.value = apiService.listCategories().filter { it.type == CATEGORY_TYPE_EXPENSE }
                _purchases.value = apiService.listPurchases(creditorId)
                _settlements.value = apiService.listSettlements(creditorId)
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load shop details.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---- Record purchase ----

    private val _showPurchaseForm = MutableStateFlow(false)
    val showPurchaseForm: StateFlow<Boolean> = _showPurchaseForm.asStateFlow()

    private val _purchaseAmount = MutableStateFlow("")
    val purchaseAmount: StateFlow<String> = _purchaseAmount.asStateFlow()

    private val _purchaseCategoryId = MutableStateFlow<Long?>(null)
    val purchaseCategoryId: StateFlow<Long?> = _purchaseCategoryId.asStateFlow()

    private val _purchaseDescription = MutableStateFlow("")
    val purchaseDescription: StateFlow<String> = _purchaseDescription.asStateFlow()

    fun openPurchaseForm() {
        _purchaseAmount.value = ""
        _purchaseCategoryId.value = _categories.value.firstOrNull()?.id
        _purchaseDescription.value = ""
        _formError.value = null
        _showPurchaseForm.value = true
    }

    fun dismissPurchaseForm() {
        _showPurchaseForm.value = false
    }

    fun onPurchaseAmountChanged(value: String) {
        _purchaseAmount.value = value
        _formError.value = null
    }

    fun onPurchaseCategorySelected(id: Long) {
        _purchaseCategoryId.value = id
        _formError.value = null
    }

    fun onPurchaseDescriptionChanged(value: String) {
        _purchaseDescription.value = value
    }

    fun submitPurchase() {
        val creditorId = detailInitializedFor ?: return
        val amount = _purchaseAmount.value.trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formError.value = "Enter a valid amount."
            return
        }
        val categoryId = _purchaseCategoryId.value
        if (categoryId == null) {
            _formError.value = "Choose a category."
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                apiService.recordPurchase(
                    creditorId,
                    RecordPurchaseRequest(amount = amount, categoryId = categoryId, description = _purchaseDescription.value.trim(), date = nowIso8601())
                )
                _showPurchaseForm.value = false
                loadDetail(creditorId)
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to record purchase.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // ---- Record settlement ----

    private val _showSettlementForm = MutableStateFlow(false)
    val showSettlementForm: StateFlow<Boolean> = _showSettlementForm.asStateFlow()

    private val _settlementAmount = MutableStateFlow("")
    val settlementAmount: StateFlow<String> = _settlementAmount.asStateFlow()

    private val _settlementAccountId = MutableStateFlow<Long?>(null)
    val settlementAccountId: StateFlow<Long?> = _settlementAccountId.asStateFlow()

    fun openSettlementForm() {
        _settlementAmount.value = ""
        _settlementAccountId.value = _accounts.value.firstOrNull()?.id
        _formError.value = null
        _showSettlementForm.value = true
    }

    fun dismissSettlementForm() {
        _showSettlementForm.value = false
    }

    fun onSettlementAmountChanged(value: String) {
        _settlementAmount.value = value
        _formError.value = null
    }

    fun onSettlementAccountSelected(id: Long) {
        _settlementAccountId.value = id
        _formError.value = null
    }

    fun submitSettlement() {
        val creditorId = detailInitializedFor ?: return
        val amount = _settlementAmount.value.trim().toDoubleOrNull()
        val outstanding = _detailCreditor.value?.outstandingDebt ?: 0L
        if (amount == null || amount <= 0) {
            _formError.value = "Enter a valid amount."
            return
        }
        if (Math.round(amount * 100) > outstanding) {
            _formError.value = "Amount exceeds the outstanding balance."
            return
        }
        val accountId = _settlementAccountId.value
        if (accountId == null) {
            _formError.value = "Choose a wallet."
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                apiService.recordSettlement(creditorId, RecordSettlementRequest(accountId = accountId, amountPaid = amount, date = nowIso8601()))
                _showSettlementForm.value = false
                loadDetail(creditorId)
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to record settlement.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
