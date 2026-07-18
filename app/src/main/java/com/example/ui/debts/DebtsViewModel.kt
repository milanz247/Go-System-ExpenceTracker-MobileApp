package com.example.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.AccountResponse
import com.example.network.ApiService
import com.example.network.CreateDebtRequest
import com.example.network.DEBT_TYPE_LENT
import com.example.network.DebtResponse
import com.example.network.RepayDebtRequest
import com.example.network.toUserMessage
import com.example.ui.common.epochMillisToIso8601
import com.example.ui.common.nowIso8601
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DebtsViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _debts = MutableStateFlow<List<DebtResponse>>(emptyList())
    val debts: StateFlow<List<DebtResponse>> = _debts.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _selectedType = MutableStateFlow(DEBT_TYPE_LENT)
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch { _userCurrency.value = dataStoreManager.currencyFlow.firstOrNull() ?: "USD" }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _debts.value = apiService.listDebts()
                _accounts.value = apiService.listAccounts().filter { it.isActive }
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load debts.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectType(type: String) {
        _selectedType.value = type
    }

    // ---- Add debt form ----

    private val _showAddForm = MutableStateFlow(false)
    val showAddForm: StateFlow<Boolean> = _showAddForm.asStateFlow()

    private val _formPersonName = MutableStateFlow("")
    val formPersonName: StateFlow<String> = _formPersonName.asStateFlow()

    private val _formTotalAmount = MutableStateFlow("")
    val formTotalAmount: StateFlow<String> = _formTotalAmount.asStateFlow()

    private val _formAccountId = MutableStateFlow<Long?>(null)
    val formAccountId: StateFlow<Long?> = _formAccountId.asStateFlow()

    private val _formDueDateMillis = MutableStateFlow(System.currentTimeMillis() + THIRTY_DAYS_MILLIS)
    val formDueDateMillis: StateFlow<Long> = _formDueDateMillis.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun openAddForm() {
        _formPersonName.value = ""
        _formTotalAmount.value = ""
        _formAccountId.value = _accounts.value.firstOrNull()?.id
        _formDueDateMillis.value = System.currentTimeMillis() + THIRTY_DAYS_MILLIS
        _formError.value = null
        _showAddForm.value = true
    }

    fun dismissAddForm() {
        _showAddForm.value = false
    }

    fun onPersonNameChanged(value: String) {
        _formPersonName.value = value
        _formError.value = null
    }

    fun onTotalAmountChanged(value: String) {
        _formTotalAmount.value = value
        _formError.value = null
    }

    fun onFormAccountSelected(id: Long) {
        _formAccountId.value = id
        _formError.value = null
    }

    fun onDueDateSelected(millis: Long) {
        _formDueDateMillis.value = millis
    }

    fun submitAddForm() {
        val name = _formPersonName.value.trim()
        if (name.isBlank()) {
            _formError.value = "Enter a name."
            return
        }
        val amount = _formTotalAmount.value.trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _formError.value = "Enter a valid amount."
            return
        }
        val accountId = _formAccountId.value
        if (accountId == null) {
            _formError.value = "Choose a wallet."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                apiService.createDebt(
                    CreateDebtRequest(
                        personName = name,
                        type = _selectedType.value,
                        totalAmount = amount,
                        accountId = accountId,
                        dueDate = epochMillisToIso8601(_formDueDateMillis.value)
                    )
                )
                _showAddForm.value = false
                load()
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to record debt.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // ---- Repay form ----

    private val _repayingDebt = MutableStateFlow<DebtResponse?>(null)
    val repayingDebt: StateFlow<DebtResponse?> = _repayingDebt.asStateFlow()

    private val _repayAmount = MutableStateFlow("")
    val repayAmount: StateFlow<String> = _repayAmount.asStateFlow()

    private val _repayAccountId = MutableStateFlow<Long?>(null)
    val repayAccountId: StateFlow<Long?> = _repayAccountId.asStateFlow()

    private val _repayError = MutableStateFlow<String?>(null)
    val repayError: StateFlow<String?> = _repayError.asStateFlow()

    fun openRepayForm(debt: DebtResponse) {
        _repayingDebt.value = debt
        _repayAmount.value = ""
        _repayAccountId.value = _accounts.value.firstOrNull()?.id
        _repayError.value = null
    }

    fun dismissRepayForm() {
        _repayingDebt.value = null
    }

    fun onRepayAmountChanged(value: String) {
        _repayAmount.value = value
        _repayError.value = null
    }

    fun onRepayAccountSelected(id: Long) {
        _repayAccountId.value = id
        _repayError.value = null
    }

    fun submitRepayForm() {
        val debt = _repayingDebt.value ?: return
        val amount = _repayAmount.value.trim().toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _repayError.value = "Enter a valid amount."
            return
        }
        if (Math.round(amount * 100) > debt.remainingAmount) {
            _repayError.value = "Amount exceeds the remaining balance."
            return
        }
        val accountId = _repayAccountId.value
        if (accountId == null) {
            _repayError.value = "Choose a wallet."
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _repayError.value = null
            try {
                apiService.repayDebt(debt.id, RepayDebtRequest(repaymentAmount = amount, accountId = accountId, date = nowIso8601()))
                _repayingDebt.value = null
                load()
            } catch (e: Exception) {
                _repayError.value = e.toUserMessage("Failed to record repayment.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    companion object {
        private const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000
    }
}
