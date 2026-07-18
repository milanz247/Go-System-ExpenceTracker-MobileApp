package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==================== Auth (backend/internal/user) ====================

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

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String
)

// ==================== Profile (backend/internal/user) ====================

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    val name: String,
    val email: String,
    val currency: String,
    val timezone: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val name: String,
    val currency: String,
    val timezone: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "old_password") val oldPassword: String,
    @Json(name = "new_password") val newPassword: String,
    @Json(name = "confirm_password") val confirmPassword: String
)

// Curated IANA zones, mirrors the web frontend's timezone picker.
val SUPPORTED_TIMEZONES = listOf(
    "UTC", "Asia/Colombo", "Asia/Kolkata", "Asia/Dhaka", "Asia/Singapore", "Asia/Tokyo",
    "Europe/London", "Europe/Berlin", "America/New_York", "America/Los_Angeles", "Australia/Sydney"
)

val SUPPORTED_CURRENCIES = listOf("LKR", "USD", "EUR")

// ==================== Accounts / Wallets (backend/internal/account) ====================

const val ACCOUNT_TYPE_BANK = "bank"
const val ACCOUNT_TYPE_CASH = "cash"
const val ACCOUNT_TYPE_CREDIT_CARD = "credit_card"
const val ACCOUNT_TYPE_INVESTMENT = "investment"
val ACCOUNT_TYPES = listOf(ACCOUNT_TYPE_BANK, ACCOUNT_TYPE_CASH, ACCOUNT_TYPE_CREDIT_CARD, ACCOUNT_TYPE_INVESTMENT)

@JsonClass(generateAdapter = true)
data class AccountResponse(
    val id: Long,
    val name: String,
    val type: String,
    val balance: Long,
    @Json(name = "credit_limit") val creditLimit: Long?,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateAccountRequest(
    val name: String,
    val type: String,
    @Json(name = "initial_balance") val initialBalance: Double,
    @Json(name = "credit_limit") val creditLimit: Double? = null
)

@JsonClass(generateAdapter = true)
data class UpdateAccountRequest(
    val name: String,
    val type: String
)

// ==================== Categories (backend/internal/category) ====================

val CATEGORY_COLORS = listOf(
    "red", "orange", "amber", "yellow", "lime", "green", "emerald", "teal", "cyan", "sky",
    "blue", "indigo", "violet", "purple", "fuchsia", "pink", "rose", "slate", "zinc", "neutral", "stone"
)

/** Tailwind-500 hex for each allowed color token; falls back to zinc-500 gray. */
fun categoryColorHex(color: String): String = when (color) {
    "red" -> "#ef4444"; "orange" -> "#f97316"; "amber" -> "#f59e0b"; "yellow" -> "#eab308"
    "lime" -> "#84cc16"; "green" -> "#22c55e"; "emerald" -> "#10b981"; "teal" -> "#14b8a6"
    "cyan" -> "#06b6d4"; "sky" -> "#0ea5e9"; "blue" -> "#3b82f6"; "indigo" -> "#6366f1"
    "violet" -> "#8b5cf6"; "purple" -> "#a855f7"; "fuchsia" -> "#d946ef"; "pink" -> "#ec4899"
    "rose" -> "#f43f5e"; else -> "#71717a"
}

val CATEGORY_ICONS = listOf(
    "TrendingUp", "TrendingDown", "Coins", "Wallet", "Utensils", "Car", "Home", "Clapperboard",
    "Zap", "HeartPulse", "ShoppingBag", "Gift", "Plane", "GraduationCap", "Briefcase",
    "Smartphone", "Dumbbell", "PawPrint", "CreditCard", "Tag", "Music", "Shirt"
)

const val CATEGORY_TYPE_INCOME = "income"
const val CATEGORY_TYPE_EXPENSE = "expense"

@JsonClass(generateAdapter = true)
data class CategoryResponse(
    val id: Long,
    val name: String,
    val type: String,
    val color: String,
    val icon: String,
    @Json(name = "is_system") val isSystem: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    val name: String,
    val type: String,
    val color: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class UpdateCategoryRequest(
    val name: String,
    val color: String,
    val icon: String
)

// ==================== Transactions (backend/internal/transaction) ====================

const val TXN_TYPE_INCOME = "income"
const val TXN_TYPE_EXPENSE = "expense"
const val TXN_TYPE_TRANSFER = "transfer"

@JsonClass(generateAdapter = true)
data class TransactionResponse(
    val id: Long,
    val type: String,
    val amount: Long,
    val fee: Long,
    @Json(name = "account_id") val accountId: Long?,
    @Json(name = "source_account_id") val sourceAccountId: Long?,
    @Json(name = "destination_account_id") val destinationAccountId: Long?,
    @Json(name = "category_id") val categoryId: Long?,
    val description: String,
    val date: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateTransactionRequest(
    val type: String,
    val amount: Double,
    val fee: Double = 0.0,
    @Json(name = "account_id") val accountId: Long? = null,
    @Json(name = "source_account_id") val sourceAccountId: Long? = null,
    @Json(name = "destination_account_id") val destinationAccountId: Long? = null,
    @Json(name = "category_id") val categoryId: Long? = null,
    val description: String,
    val date: String
)

// ==================== Debts (backend/internal/debt) ====================

const val DEBT_TYPE_LENT = "lent"
const val DEBT_TYPE_BORROWED = "borrowed"
const val DEBT_STATUS_PENDING = "pending"
const val DEBT_STATUS_SETTLED = "settled"

@JsonClass(generateAdapter = true)
data class DebtResponse(
    val id: Long,
    @Json(name = "person_name") val personName: String,
    val type: String,
    @Json(name = "total_amount") val totalAmount: Long,
    @Json(name = "remaining_amount") val remainingAmount: Long,
    val status: String,
    @Json(name = "due_date") val dueDate: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateDebtRequest(
    @Json(name = "person_name") val personName: String,
    val type: String,
    @Json(name = "total_amount") val totalAmount: Double,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "due_date") val dueDate: String
)

@JsonClass(generateAdapter = true)
data class RepayDebtRequest(
    @Json(name = "repayment_amount") val repaymentAmount: Double,
    @Json(name = "account_id") val accountId: Long,
    val date: String
)

// ==================== Store Tabs / Credit Books (backend/internal/store) ====================

@JsonClass(generateAdapter = true)
data class CreditorResponse(
    val id: Long,
    val name: String,
    @Json(name = "outstanding_debt") val outstandingDebt: Long,
    @Json(name = "is_active") val isActive: Boolean,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class CreateCreditorRequest(
    val name: String
)

@JsonClass(generateAdapter = true)
data class PurchaseResponse(
    val id: Long,
    @Json(name = "creditor_id") val creditorId: Long,
    val amount: Long,
    val fee: Long,
    @Json(name = "category_id") val categoryId: Long,
    val description: String,
    val date: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class RecordPurchaseRequest(
    val amount: Double,
    val fee: Double = 0.0,
    @Json(name = "category_id") val categoryId: Long,
    val description: String,
    val date: String
)

@JsonClass(generateAdapter = true)
data class SettlementResponse(
    val id: Long,
    @Json(name = "creditor_id") val creditorId: Long,
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "amount_paid") val amountPaid: Long,
    val fee: Long,
    val date: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class RecordSettlementRequest(
    @Json(name = "account_id") val accountId: Long,
    @Json(name = "amount_paid") val amountPaid: Double,
    val fee: Double = 0.0,
    val date: String
)

// ==================== Reports / Dashboard (backend/internal/report) ====================

@JsonClass(generateAdapter = true)
data class MetricWithTrend(
    @Json(name = "amount_cents") val amountCents: Long,
    @Json(name = "change_percent") val changePercent: Double?
)

@JsonClass(generateAdapter = true)
data class CashFlowPoint(
    val month: String,
    val income: Long,
    val expense: Long
)

@JsonClass(generateAdapter = true)
data class CategoryBreakdownItem(
    @Json(name = "category_name") val categoryName: String,
    @Json(name = "amount_cents") val amountCents: Long,
    val percentage: Double,
    val color: String
)

@JsonClass(generateAdapter = true)
data class RecentTransactionCategory(
    val name: String,
    val color: String,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class RecentTransaction(
    val id: Long,
    val type: String,
    val amount: Long,
    val description: String,
    val date: String,
    val category: RecentTransactionCategory?
)

@JsonClass(generateAdapter = true)
data class SummaryResponse(
    @Json(name = "total_income") val totalIncome: MetricWithTrend,
    @Json(name = "total_expense") val totalExpense: MetricWithTrend,
    @Json(name = "net_balance") val netBalance: MetricWithTrend,
    @Json(name = "cash_flow") val cashFlow: List<CashFlowPoint>,
    @Json(name = "category_breakdown") val categoryBreakdown: List<CategoryBreakdownItem>,
    @Json(name = "recent_transactions") val recentTransactions: List<RecentTransaction>
)
