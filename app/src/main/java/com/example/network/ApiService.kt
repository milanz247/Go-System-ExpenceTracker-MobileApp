package com.example.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** All paths are relative to [BuildConfig.API_BASE_URL] which already ends in `/api/v1/`. */
interface ApiService {

    // ---- Auth ----
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // ---- Profile ----
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ProfileResponse

    @POST("profile/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): MessageResponse

    // ---- Accounts / Wallets ----
    @POST("accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): AccountResponse

    @GET("accounts")
    suspend fun listAccounts(): List<AccountResponse>

    @PUT("accounts/{id}")
    suspend fun updateAccount(@Path("id") id: Long, @Body request: UpdateAccountRequest): AccountResponse

    /** Soft-delete (archive). Response shape isn't load-bearing here, so the raw body is untyped. */
    @DELETE("accounts/{id}")
    suspend fun archiveAccount(@Path("id") id: Long): Response<ResponseBody>

    // ---- Categories ----
    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): CategoryResponse

    @GET("categories")
    suspend fun listCategories(): List<CategoryResponse>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body request: UpdateCategoryRequest): CategoryResponse

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): Response<ResponseBody>

    // ---- Transactions ----
    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionResponse

    @GET("transactions")
    suspend fun listTransactions(@Query("account_id") accountId: Long? = null): List<TransactionResponse>

    // ---- Debts ----
    @POST("debts")
    suspend fun createDebt(@Body request: CreateDebtRequest): DebtResponse

    @GET("debts")
    suspend fun listDebts(): List<DebtResponse>

    @POST("debts/{id}/repay")
    suspend fun repayDebt(@Path("id") id: Long, @Body request: RepayDebtRequest): DebtResponse

    // ---- Store Tabs / Credit Books ----
    @POST("store-creditors")
    suspend fun createStore(@Body request: CreateCreditorRequest): CreditorResponse

    @GET("store-creditors")
    suspend fun listStores(): List<CreditorResponse>

    @POST("store-creditors/{id}/purchases")
    suspend fun recordPurchase(@Path("id") id: Long, @Body request: RecordPurchaseRequest): PurchaseResponse

    @GET("store-creditors/{id}/purchases")
    suspend fun listPurchases(@Path("id") id: Long): List<PurchaseResponse>

    @POST("store-creditors/{id}/settlements")
    suspend fun recordSettlement(@Path("id") id: Long, @Body request: RecordSettlementRequest): SettlementResponse

    @GET("store-creditors/{id}/settlements")
    suspend fun listSettlements(@Path("id") id: Long): List<SettlementResponse>

    // ---- Reports / Dashboard ----
    @GET("reports/summary")
    suspend fun getSummary(): SummaryResponse
}
