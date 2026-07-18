package com.example.network

import android.content.Context
import com.example.BuildConfig
import com.example.data.DataStoreManager
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.IOException
import java.util.concurrent.TimeUnit

// ---- Auth DTOs — field names mirror backend/internal/user/dto.go exactly ----

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val currency: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    val id: Long,
    val name: String,
    val email: String,
    val currency: String
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthenticatedUser(
    val name: String,
    val currency: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: AuthenticatedUser
)

/** Matches the Go backend's `errorResponse{ Error string \`json:"error"\` }` shape. */
@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    val error: String
)

// ---- Placeholder dashboard DTOs. The real backend has no single "/dashboard"
// endpoint (it's /reports/summary + /accounts + /transactions); wiring those
// up properly is a later step, so this intentionally 404s against the real
// API for now and DashboardViewModel already surfaces that as a sync error. ----

@JsonClass(generateAdapter = true)
data class Wallet(
    val id: String,
    val name: String,
    val balance: Double,
    val currency: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val date: String,
    val walletId: String
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val wallets: List<Wallet>,
    val transactions: List<Transaction>
)

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("dashboard")
    suspend fun getDashboardData(): DashboardResponse
}

object NetworkClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val errorAdapter = moshi.adapter(ApiErrorResponse::class.java)

    fun getApiService(context: Context, dataStoreManager: DataStoreManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(dataStoreManager))
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ApiService::class.java)
    }

    /** Best-effort extraction of the Go backend's `{"error": "..."}` body from a failed call. */
    fun parseErrorBody(exception: HttpException): String? {
        val raw = exception.response()?.errorBody()?.string()
        if (raw.isNullOrBlank()) return null
        return runCatching { errorAdapter.fromJson(raw)?.error }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}

/** Turns any exception from an [ApiService] call into a message safe to show a user. */
fun Throwable.toUserMessage(fallback: String = "Something went wrong. Please try again."): String =
    when (this) {
        is HttpException -> NetworkClient.parseErrorBody(this) ?: fallback
        is IOException -> "Can't reach the server. Check your connection and try again."
        else -> fallback
    }
