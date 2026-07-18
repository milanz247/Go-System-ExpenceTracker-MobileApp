package com.example.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.AccountResponse
import com.example.network.CashFlowPoint
import com.example.network.CategoryBreakdownItem
import com.example.network.MetricWithTrend
import com.example.network.RecentTransaction
import com.example.network.TXN_TYPE_EXPENSE
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.formatMoney
import com.example.ui.common.formatMoneyCompact
import com.example.ui.common.parseHexColor
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeeAllTransactions: () -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Dashboard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                        Text(
                            text = if (userName.isBlank()) "Welcome back" else "Welcome back, $userName",
                            fontSize = 13.sp,
                            color = Zinc500
                        )
                    }
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Zinc500)
                    }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && summary == null) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            }

            summary?.let { s ->
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Income", s.totalIncome, userCurrency, Modifier.weight(1f))
                        MetricCard("Expense", s.totalExpense, userCurrency, Modifier.weight(1f))
                        MetricCard("Net", s.netBalance, userCurrency, Modifier.weight(1f))
                    }
                }

                if (accounts.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionLabel("Wallets")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(accounts) { account -> WalletChip(account, userCurrency) }
                            }
                        }
                    }
                }

                if (s.cashFlow.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionLabel("Cash Flow")
                            MatteCard { CashFlowBarChart(s.cashFlow) }
                        }
                    }
                }

                if (s.categoryBreakdown.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionLabel("Spend by Category")
                            MatteCard { CategoryBreakdownRow(s.categoryBreakdown) }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionLabel("Recent Activity")
                            Text(
                                text = "See all",
                                fontSize = 12.sp,
                                color = Zinc400,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { onSeeAllTransactions() }
                            )
                        }
                        if (s.recentTransactions.isEmpty()) {
                            EmptyState("No transactions yet.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                s.recentTransactions.forEach { RecentActivityRow(it, userCurrency) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, metric: MetricWithTrend, currency: String, modifier: Modifier = Modifier) {
    MatteCard(modifier = modifier, cornerRadius = 20, contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = label.uppercase(), fontSize = 10.sp, color = Zinc500, letterSpacing = 1.sp, fontFamily = GeistMono)
            Text(
                text = formatMoneyCompact(metric.amountCents, currency),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite,
                fontFamily = GeistMono,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val change = metric.changePercent
            if (change != null) {
                val positive = change >= 0
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (positive) Color(0xFF22C55E) else Color(0xFFF43F5E),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${String.format("%.1f", Math.abs(change))}%",
                        fontSize = 11.sp,
                        color = if (positive) Color(0xFF22C55E) else Color(0xFFF43F5E)
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletChip(account: AccountResponse, currency: String) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Zinc900)
            .border(1.dp, Zinc800, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = account.name,
                fontSize = 12.sp,
                color = Zinc400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatMoneyCompact(account.balance, currency),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                fontFamily = GeistMono
            )
        }
    }
}

@Composable
private fun CashFlowBarChart(data: List<CashFlowPoint>, modifier: Modifier = Modifier) {
    val maxValue = remember(data) { (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        data.forEach { point ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(fraction = (point.income.toFloat() / maxValue).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(PureWhite)
                    )
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(fraction = (point.expense.toFloat() / maxValue).coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(Zinc700)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = point.month, fontSize = 10.sp, color = Zinc500, fontFamily = GeistMono)
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(data: List<CategoryBreakdownItem>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Canvas(modifier = Modifier.size(96.dp)) {
            var startAngle = -90f
            val strokeWidth = size.minDimension * 0.22f
            data.forEach { item ->
                val sweep = (item.percentage / 100f * 360f).toFloat().coerceAtLeast(0f)
                drawArc(
                    color = parseHexColor(item.color),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth)
                )
                startAngle += sweep
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            data.take(6).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(item.color))
                    )
                    Text(
                        text = item.categoryName,
                        fontSize = 12.sp,
                        color = Zinc400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${String.format("%.0f", item.percentage)}%",
                        fontSize = 12.sp,
                        color = PureWhite,
                        fontFamily = GeistMono
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(transaction: RecentTransaction, currency: String) {
    val isExpense = transaction.type == TXN_TYPE_EXPENSE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Zinc900)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(transaction.category?.color?.let { parseHexColor(it).copy(alpha = 0.2f) } ?: Zinc800),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(transaction.category?.color?.let { parseHexColor(it) } ?: Zinc500)
                )
            }
            Column {
                Text(
                    text = transaction.description.ifBlank { transaction.category?.name ?: transaction.type },
                    fontSize = 14.sp,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.category?.name ?: transaction.type,
                    fontSize = 11.sp,
                    color = Zinc500
                )
            }
        }
        Text(
            text = (if (isExpense) "-" else "+") + formatMoney(transaction.amount, currency),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isExpense) Zinc400 else PureWhite,
            fontFamily = GeistMono
        )
    }
}
