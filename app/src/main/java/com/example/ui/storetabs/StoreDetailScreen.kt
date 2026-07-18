package com.example.ui.storetabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.PurchaseResponse
import com.example.network.SettlementResponse
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.MatteCard
import com.example.ui.common.SectionLabel
import com.example.ui.common.formatDisplayDate
import com.example.ui.common.formatMoney
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    viewModel: StoreTabsViewModel,
    creditorId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(creditorId) { viewModel.ensureDetailLoaded(creditorId) }

    val creditor by viewModel.detailCreditor.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showPurchaseForm by viewModel.showPurchaseForm.collectAsState()
    val showSettlementForm by viewModel.showSettlementForm.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(PitchBlack).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
                    }
                    Text(
                        text = creditor?.name ?: "Shop",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            creditor?.let {
                item {
                    MatteCard {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "OUTSTANDING", fontSize = 10.sp, color = Zinc500, letterSpacing = 1.sp, fontFamily = GeistMono)
                            Text(text = formatMoney(it.outstandingDebt, currency), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PureWhite, fontFamily = GeistMono)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.openPurchaseForm() },
                        colors = ButtonDefaults.buttonColors(containerColor = Zinc900, contentColor = PureWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Record Purchase", fontSize = 12.sp) }
                    Button(
                        onClick = { viewModel.openSettlementForm() },
                        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PitchBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) { Text("Settle Up", fontSize = 12.sp) }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && purchases.isEmpty() && settlements.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(160.dp)) }
            } else {
                item { SectionLabel("Purchases") }
                if (purchases.isEmpty()) {
                    item { EmptyState("No purchases recorded yet.") }
                } else {
                    items(purchases, key = { "p${it.id}" }) { PurchaseRow(it, currency) }
                }

                item { SectionLabel("Settlements") }
                if (settlements.isEmpty()) {
                    item { EmptyState("No settlements recorded yet.") }
                } else {
                    items(settlements, key = { "s${it.id}" }) { SettlementRow(it, currency) }
                }
            }
        }
    }

    if (showPurchaseForm) {
        RecordPurchaseDialog(viewModel)
    }
    if (showSettlementForm) {
        RecordSettlementDialog(viewModel, currency)
    }
}

@Composable
private fun PurchaseRow(purchase: PurchaseResponse, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Zinc900, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = purchase.description.ifBlank { "Purchase" }, fontSize = 13.sp, color = PureWhite)
            Text(text = formatDisplayDate(purchase.date), fontSize = 11.sp, color = Zinc500)
        }
        Text(text = "+${formatMoney(purchase.amount + purchase.fee, currency)}", fontSize = 13.sp, color = Zinc400, fontFamily = GeistMono)
    }
}

@Composable
private fun SettlementRow(settlement: SettlementResponse, currency: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Zinc900, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = formatDisplayDate(settlement.date), fontSize = 12.sp, color = Zinc500)
        Text(text = "-${formatMoney(settlement.amountPaid, currency)}", fontSize = 13.sp, color = PureWhite, fontFamily = GeistMono)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordPurchaseDialog(viewModel: StoreTabsViewModel) {
    val amount by viewModel.purchaseAmount.collectAsState()
    val categoryId by viewModel.purchaseCategoryId.collectAsState()
    val description by viewModel.purchaseDescription.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissPurchaseForm() },
        containerColor = com.example.ui.theme.Zinc950,
        titleContentColor = PureWhite,
        textContentColor = Zinc400,
        title = { Text("Record Purchase") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::onPurchaseAmountChanged,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == categoryId }?.name ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }, containerColor = Zinc900) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name, color = PureWhite) }, onClick = { viewModel.onPurchaseCategorySelected(cat.id); categoryMenuExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = viewModel::onPurchaseDescriptionChanged,
                    label = { Text("Description") },
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (formError != null) {
                    Text(text = formError!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitPurchase() }, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PureWhite)
                else Text("Save", color = PureWhite)
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.dismissPurchaseForm() }, enabled = !isSubmitting) { Text("Cancel", color = Zinc500) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSettlementDialog(viewModel: StoreTabsViewModel, currency: String) {
    val amount by viewModel.settlementAmount.collectAsState()
    val accountId by viewModel.settlementAccountId.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val creditor by viewModel.detailCreditor.collectAsState()
    var accountMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissSettlementForm() },
        containerColor = com.example.ui.theme.Zinc950,
        titleContentColor = PureWhite,
        textContentColor = Zinc400,
        title = { Text("Settle Up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(text = "Outstanding: ${formatMoney(creditor?.outstandingDebt ?: 0L, currency)}", fontSize = 13.sp, color = Zinc400)
                OutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::onSettlementAmountChanged,
                    label = { Text("Amount to Pay") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = accountMenuExpanded, onExpandedChange = { accountMenuExpanded = it }) {
                    OutlinedTextField(
                        value = accounts.find { it.id == accountId }?.name ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pay From") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountMenuExpanded) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = accountMenuExpanded, onDismissRequest = { accountMenuExpanded = false }, containerColor = Zinc900) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(text = { Text(acc.name, color = PureWhite) }, onClick = { viewModel.onSettlementAccountSelected(acc.id); accountMenuExpanded = false })
                        }
                    }
                }
                if (formError != null) {
                    Text(text = formError!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitSettlement() }, enabled = !isSubmitting) {
                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PureWhite)
                else Text("Confirm", color = PureWhite)
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.dismissSettlementForm() }, enabled = !isSubmitting) { Text("Cancel", color = Zinc500) } }
    )
}
