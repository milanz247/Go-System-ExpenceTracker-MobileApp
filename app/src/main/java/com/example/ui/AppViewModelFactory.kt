package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.DataStoreManager
import com.example.network.ApiService
import com.example.ui.auth.AuthViewModel
import com.example.ui.categories.CategoriesViewModel
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.debts.DebtsViewModel
import com.example.ui.profile.ProfileViewModel
import com.example.ui.storetabs.StoreTabsViewModel
import com.example.ui.transactions.TransactionsViewModel
import com.example.ui.wallets.WalletsViewModel

/** Single factory for every screen ViewModel — all share the same [apiService]/[dataStoreManager]. */
class AppViewModelFactory(
    private val apiService: ApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val instance: ViewModel = when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(WalletsViewModel::class.java) -> WalletsViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(CategoriesViewModel::class.java) -> CategoriesViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(TransactionsViewModel::class.java) -> TransactionsViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(DebtsViewModel::class.java) -> DebtsViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(StoreTabsViewModel::class.java) -> StoreTabsViewModel(apiService, dataStoreManager)
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(apiService, dataStoreManager)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return instance as T
    }
}
