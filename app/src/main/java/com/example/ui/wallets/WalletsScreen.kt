package com.example.ui.wallets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.ACCOUNT_TYPES
import com.example.network.ACCOUNT_TYPE_CREDIT_CARD
import com.example.network.AccountResponse
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorBanner
import com.example.ui.common.FullScreenLoader
import com.example.ui.common.formatMoney
import com.example.ui.theme.GeistMono
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900
import com.example.ui.theme.Zinc950

private val fieldColors: @Composable () -> TextFieldColors = {
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = PureWhite, unfocusedTextColor = PureWhite,
        focusedBorderColor = PureWhite, unfocusedBorderColor = Zinc800,
        focusedLabelColor = PureWhite, unfocusedLabelColor = Zinc400,
        cursorColor = PureWhite
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModel: WalletsViewModel,
    onWalletClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val currency by viewModel.userCurrency.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showForm by viewModel.showForm.collectAsState()
    val pendingArchive by viewModel.accountPendingArchive.collectAsState()

    Box(modifier = modifier.fillMaxSize().background(PitchBlack).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Wallets", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    IconButton(onClick = { viewModel.openAddForm() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add wallet", tint = PureWhite)
                    }
                }
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }

            if (isLoading && accounts.isEmpty()) {
                item { FullScreenLoader(modifier = Modifier.fillMaxWidth().height(200.dp)) }
            } else if (accounts.isEmpty()) {
                item { EmptyState("No wallets yet. Tap + to add your first one.") }
            } else {
                items(accounts, key = { it.id }) { account ->
                    WalletCard(
                        account = account,
                        currency = currency,
                        onClick = { onWalletClick(account.id) },
                        onEdit = { viewModel.openEditForm(account) },
                        onArchive = { viewModel.requestArchive(account) }
                    )
                }
            }
        }
    }

    if (showForm) {
        WalletFormDialog(viewModel)
    }

    pendingArchive?.let { account ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelArchive() },
            containerColor = Zinc950,
            titleContentColor = PureWhite,
            textContentColor = Zinc400,
            title = { Text("Archive \"${account.name}\"?") },
            text = { Text("This hides the wallet from your active list. Its transaction history is kept.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmArchive() }) { Text("Archive", color = PureWhite) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelArchive() }) { Text("Cancel", color = Zinc500) }
            }
        )
    }
}

@Composable
private fun WalletCard(
    account: AccountResponse,
    currency: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Zinc950)
            .border(1.dp, Zinc800, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = account.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = account.type.replace('_', ' ').uppercase(),
                    fontSize = 10.sp,
                    color = Zinc500,
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatMoney(account.balance, currency),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite,
                    fontFamily = GeistMono
                )
                if (account.type == ACCOUNT_TYPE_CREDIT_CARD && account.creditLimit != null) {
                    Text(
                        text = "Limit ${formatMoney(account.creditLimit, currency)}",
                        fontSize = 11.sp,
                        color = Zinc500
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Wallet options", tint = Zinc500)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, containerColor = Zinc900) {
                    DropdownMenuItem(text = { Text("Edit", color = PureWhite) }, onClick = { menuExpanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Archive", color = PureWhite) }, onClick = { menuExpanded = false; onArchive() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletFormDialog(viewModel: WalletsViewModel) {
    val name by viewModel.formName.collectAsState()
    val type by viewModel.formType.collectAsState()
    val initialBalance by viewModel.formInitialBalance.collectAsState()
    val creditLimit by viewModel.formCreditLimit.collectAsState()
    val formError by viewModel.formError.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val editingId by viewModel.editingAccountId.collectAsState()
    val isCreate = editingId == null

    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) viewModel.dismissForm() },
        containerColor = Zinc950,
        titleContentColor = PureWhite,
        textContentColor = Zinc400,
        title = { Text(if (isCreate) "Add Wallet" else "Edit Wallet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Name") },
                    singleLine = true,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = it }) {
                    OutlinedTextField(
                        value = type.replace('_', ' ').replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }, containerColor = Zinc900) {
                        ACCOUNT_TYPES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.replace('_', ' ').replaceFirstChar { it.uppercase() }, color = PureWhite) },
                                onClick = { viewModel.onTypeSelected(option); typeMenuExpanded = false }
                            )
                        }
                    }
                }

                if (isCreate) {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = viewModel::onInitialBalanceChanged,
                        label = { Text("Starting Balance") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (type == ACCOUNT_TYPE_CREDIT_CARD) {
                        OutlinedTextField(
                            value = creditLimit,
                            onValueChange = viewModel::onCreditLimitChanged,
                            label = { Text("Credit Limit") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (formError != null) {
                    Text(text = formError!!, color = androidx.compose.ui.graphics.Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.submitForm() }, enabled = !isSubmitting) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PureWhite)
                } else {
                    Text(if (isCreate) "Create" else "Save", color = PureWhite)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.dismissForm() }, enabled = !isSubmitting) {
                Text("Cancel", color = Zinc500)
            }
        }
    )
}
