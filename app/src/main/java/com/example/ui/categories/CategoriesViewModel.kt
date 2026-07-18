package com.example.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DataStoreManager
import com.example.network.CATEGORY_COLORS
import com.example.network.CATEGORY_ICONS
import com.example.network.CATEGORY_TYPE_EXPENSE
import com.example.network.CATEGORY_TYPE_INCOME
import com.example.network.ApiService
import com.example.network.CategoryResponse
import com.example.network.CreateCategoryRequest
import com.example.network.UpdateCategoryRequest
import com.example.network.requireSuccess
import com.example.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _selectedType = MutableStateFlow(CATEGORY_TYPE_EXPENSE)
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showForm = MutableStateFlow(false)
    val showForm: StateFlow<Boolean> = _showForm.asStateFlow()

    private val _editingCategoryId = MutableStateFlow<Long?>(null)
    val editingCategoryId: StateFlow<Long?> = _editingCategoryId.asStateFlow()

    private val _formName = MutableStateFlow("")
    val formName: StateFlow<String> = _formName.asStateFlow()

    private val _formType = MutableStateFlow(CATEGORY_TYPE_EXPENSE)
    val formType: StateFlow<String> = _formType.asStateFlow()

    private val _formColor = MutableStateFlow(CATEGORY_COLORS.first())
    val formColor: StateFlow<String> = _formColor.asStateFlow()

    private val _formIcon = MutableStateFlow(CATEGORY_ICONS.first())
    val formIcon: StateFlow<String> = _formIcon.asStateFlow()

    private val _formError = MutableStateFlow<String?>(null)
    val formError: StateFlow<String?> = _formError.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _categoryPendingDelete = MutableStateFlow<CategoryResponse?>(null)
    val categoryPendingDelete: StateFlow<CategoryResponse?> = _categoryPendingDelete.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _categories.value = apiService.listCategories()
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("Failed to load categories.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectType(type: String) {
        _selectedType.value = type
    }

    fun categoriesForSelectedType(): List<CategoryResponse> =
        _categories.value.filter { it.type == _selectedType.value }

    fun openAddForm() {
        _editingCategoryId.value = null
        _formName.value = ""
        _formType.value = _selectedType.value
        _formColor.value = CATEGORY_COLORS.first()
        _formIcon.value = CATEGORY_ICONS.first()
        _formError.value = null
        _showForm.value = true
    }

    fun openEditForm(category: CategoryResponse) {
        _editingCategoryId.value = category.id
        _formName.value = category.name
        _formType.value = category.type
        _formColor.value = category.color
        _formIcon.value = category.icon
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

    fun onColorSelected(value: String) {
        _formColor.value = value
    }

    fun onIconSelected(value: String) {
        _formIcon.value = value
    }

    fun submitForm() {
        val name = _formName.value.trim()
        if (name.isBlank()) {
            _formError.value = "Name cannot be empty."
            return
        }
        val editingId = _editingCategoryId.value

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                if (editingId == null) {
                    apiService.createCategory(CreateCategoryRequest(name, _formType.value, _formColor.value, _formIcon.value))
                } else {
                    apiService.updateCategory(editingId, UpdateCategoryRequest(name, _formColor.value, _formIcon.value))
                }
                _showForm.value = false
                load()
            } catch (e: Exception) {
                _formError.value = e.toUserMessage("Failed to save category.")
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun requestDelete(category: CategoryResponse) {
        _categoryPendingDelete.value = category
    }

    fun cancelDelete() {
        _categoryPendingDelete.value = null
    }

    fun confirmDelete() {
        val category = _categoryPendingDelete.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                apiService.deleteCategory(category.id).requireSuccess()
                _categoryPendingDelete.value = null
                load()
            } catch (e: Exception) {
                _errorMessage.value = e.toUserMessage("This category can't be deleted — it may still be in use.")
                _categoryPendingDelete.value = null
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
