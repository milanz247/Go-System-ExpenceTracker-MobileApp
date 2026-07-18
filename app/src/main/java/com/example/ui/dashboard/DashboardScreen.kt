package com.example.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.Transaction
import com.example.network.Wallet
import com.example.ui.theme.MatteBlack
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc100
import com.example.ui.theme.Zinc200
import com.example.ui.theme.Zinc300
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import com.example.ui.theme.Zinc950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // AI advisor states
    val aiQuery by viewModel.aiQuery.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    // Total balance & metrics calculations
    val totalBalance = remember(wallets) {
        wallets.sumOf { it.balance }
    }
    val totalIncome = remember(transactions) {
        transactions.filter { it.type.uppercase() == "INCOME" }.sumOf { it.amount }
    }
    val totalExpenses = remember(transactions) {
        transactions.filter { it.type.uppercase() == "EXPENSE" }.sumOf { Math.abs(it.amount) }
    }

    // Navigation trigger
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is DashboardViewModel.DashboardEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Dashboard",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = PureWhite,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.testTag("welcome_text")
                        )

                        // Avatar Widget (Tailwind bg-zinc-900 border border-zinc-800 flex items-center justify-center)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Zinc900)
                                .border(1.dp, Zinc800, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(2).uppercase(),
                                color = Zinc400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Welcome back, $userName",
                            color = Zinc500,
                            fontSize = 14.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { viewModel.loadDashboardData() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync system",
                                    tint = Zinc500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "•",
                                color = Zinc700,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Disconnect",
                                color = Zinc500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { viewModel.logout() }
                                    .testTag("logout_button")
                            )
                        }
                    }
                }
            }

            // Sync error banner
            if (errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Zinc950)
                            .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                            .testTag("sync_error_banner")
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Total Balance Card (Clean Minimalism Capsule Box)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(Zinc950)
                        .border(1.dp, Zinc800, RoundedCornerShape(32.dp))
                        .padding(28.dp)
                        .testTag("total_balance_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "NET WORTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 2.5.sp,
                                color = Zinc500,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format("%s %,.2f", userCurrency, totalBalance),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite,
                                letterSpacing = (-1.5).sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        // Split detail section with Income and Expenses
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "INCOME",
                                    fontSize = 10.sp,
                                    color = Zinc500,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("+%s %,.0f", userCurrency, totalIncome),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PureWhite
                                )
                            }

                            // Center border separator
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(Zinc800)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EXPENSES",
                                    fontSize = 10.sp,
                                    color = Zinc500,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format("-%s %,.0f", userCurrency, totalExpenses),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }

            // Active Wallet Systems
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE WALLETS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Zinc400,
                            letterSpacing = 1.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "View all",
                            fontSize = 11.sp,
                            color = Zinc500,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (wallets.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Zinc950)
                                .border(1.dp, Zinc800, RoundedCornerShape(24.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Zinc700,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = "No active ledger accounts detected.",
                                    color = Zinc500,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Alternate background cards (odd = Zinc900 with subtle borders, even = PureWhite with high contrast text)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            wallets.forEachIndexed { index, wallet ->
                                Box(modifier = Modifier.weight(1f)) {
                                    WalletItemCard(wallet = wallet, isPrimaryContrast = (index % 2 == 1))
                                }
                            }
                        }
                    }
                }
            }

            // Historical Ledger Listings (Recent Transactions)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RECENT ACTIVITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Zinc400,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Zinc950)
                                .border(1.dp, Zinc800, RoundedCornerShape(24.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No ledger entries recorded yet.",
                                color = Zinc500,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            transactions.forEach { transaction ->
                                TransactionItemRow(transaction = transaction)
                            }
                        }
                    }
                }
            }

            // High-Thinking AI Financial Advisor (Gemini Core integration)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Zinc950)
                        .border(1.dp, Zinc800, RoundedCornerShape(24.dp))
                        .padding(24.dp)
                        .testTag("ai_advisor_section")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "AI INTEL ADVISOR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                letterSpacing = 1.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Query the model with your custom requests. Operates with gemini-3.1-pro-preview in HIGH-THINKING mode to structure budgets, calculate savings, or perform complex margin optimizations.",
                            fontSize = 12.sp,
                            color = Zinc400,
                            lineHeight = 16.sp
                        )

                        // Input Field
                        OutlinedTextField(
                            value = aiQuery,
                            onValueChange = viewModel::onAiQueryChanged,
                            label = { Text("Enter prompt / strategy target") },
                            placeholder = { Text("e.g. Draft an investment blueprint...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = Zinc850Color(),
                                focusedLabelColor = PureWhite,
                                unfocusedLabelColor = Zinc400,
                                cursorColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_query_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Action Button
                        Button(
                            onClick = { viewModel.askAiAdvisor() },
                            enabled = !isAiThinking && aiQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PureWhite,
                                contentColor = PitchBlack,
                                disabledContainerColor = Zinc800,
                                disabledContentColor = Zinc400
                    ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("ai_query_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "ENGAGE THINKING PROCESS",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 12.sp
                            )
                        }

                        // Thinking Scanner Loader
                        AnimatedVisibility(
                            visible = isAiThinking,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearThinkingProgressIndicator()
                                Text(
                                    text = "GEMINI IS DEEP THINKING (HIGH LEVEL)... PLEASE WAIT",
                                    fontSize = 10.sp,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Response Output Block
                        AnimatedVisibility(
                            visible = aiResponse != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            aiResponse?.let { text ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(PitchBlack)
                                        .border(1.dp, Zinc800, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                        .testTag("ai_response_box")
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "ADVISORY STRATEGY OUTPUT:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Zinc500,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = text,
                                            fontSize = 13.sp,
                                            color = Zinc200,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        // AI Error Banner
                        AnimatedVisibility(
                            visible = aiError != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            aiError?.let { err ->
                                Text(
                                    text = err,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ai_error_banner")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper color for cleaner outlines
@Composable
fun Zinc850Color() = Color(0xFF1F1F23)

@Composable
fun WalletItemCard(wallet: Wallet, isPrimaryContrast: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isPrimaryContrast) PureWhite else Zinc900)
            .border(
                1.dp,
                if (isPrimaryContrast) Color.Transparent else Zinc800,
                RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
            .testTag("wallet_card_${wallet.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isPrimaryContrast) Zinc100 else PitchBlack)
                    .border(
                        1.dp,
                        if (isPrimaryContrast) Color.Transparent else Zinc700,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = wallet.name.take(1).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimaryContrast) PitchBlack else PureWhite
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = wallet.name,
                    fontSize = 12.sp,
                    color = if (isPrimaryContrast) Zinc700 else Zinc500,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = String.format("%s%,.0f", wallet.currency, wallet.balance),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimaryContrast) PitchBlack else PureWhite,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}

@Composable
fun TransactionItemRow(transaction: Transaction) {
    val isExpense = transaction.type.uppercase() == "EXPENSE"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Zinc950)
            .border(1.dp, Zinc900, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("transaction_row_${transaction.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Category-Specific Decorative Emojis
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Zinc900),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (transaction.category.uppercase()) {
                        "FOOD", "DINING", "CAFE" -> "☕"
                        "WELLNESS", "FITNESS", "HEALTH" -> "🧘"
                        "DEVOPS", "TECH", "CLOUD", "SOFTWARE" -> "💻"
                        "REVENUE", "SALARY", "BONUS" -> "💰"
                        "INVESTING", "STOCKS" -> "📈"
                        "RENT", "HOUSING" -> "🏠"
                        else -> "💵"
                    },
                    fontSize = 18.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category,
                        fontSize = 11.sp,
                        color = Zinc500
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(Zinc700)
                    )
                    Text(
                        text = transaction.date,
                        fontSize = 11.sp,
                        color = Zinc500
                    )
                }
            }
        }

        Text(
            text = String.format("%s%s%,.2f", if (isExpense) "-" else "+", if (transaction.amount < 0) "" else "", Math.abs(transaction.amount)),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) Zinc300 else PureWhite,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun LinearThinkingProgressIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val translationX by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "translation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Zinc900)
            .clip(RoundedCornerShape(1.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.3f)
                .align(Alignment.CenterStart)
                .offset(x = (translationX * 300).dp) // simple approximate offset movement
                .background(PureWhite)
        )
    }
}
