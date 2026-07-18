package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.SUPPORTED_CURRENCIES
import com.example.network.SUPPORTED_TIMEZONES
import com.example.ui.common.ErrorBanner
import com.example.ui.common.MatteCard
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
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onManageCategories: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { viewModel.ensureLoaded() }

    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val timezone by viewModel.timezone.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveMessage by viewModel.saveMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var currencyMenuExpanded by remember { mutableStateOf(false) }
    var timezoneMenuExpanded by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(PitchBlack).windowInsetsPadding(WindowInsets.safeDrawing)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Text(text = "Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PureWhite)
            }

            if (errorMessage != null) {
                item { ErrorBanner(errorMessage!!) }
            }
            if (saveMessage != null) {
                item { Text(text = saveMessage!!, color = Zinc400, fontSize = 13.sp) }
            }

            item {
                MatteCard {
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
                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Email") },
                            singleLine = true,
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        ExposedDropdownMenuBox(expanded = currencyMenuExpanded, onExpandedChange = { currencyMenuExpanded = it }) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyMenuExpanded) },
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = currencyMenuExpanded, onDismissRequest = { currencyMenuExpanded = false }, containerColor = Zinc900) {
                                SUPPORTED_CURRENCIES.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = PureWhite) }, onClick = { viewModel.onCurrencySelected(option); currencyMenuExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(expanded = timezoneMenuExpanded, onExpandedChange = { timezoneMenuExpanded = it }) {
                            OutlinedTextField(
                                value = timezone,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Timezone") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timezoneMenuExpanded) },
                                colors = fieldColors(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(expanded = timezoneMenuExpanded, onDismissRequest = { timezoneMenuExpanded = false }, containerColor = Zinc900) {
                                SUPPORTED_TIMEZONES.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = PureWhite) }, onClick = { viewModel.onTimezoneSelected(option); timezoneMenuExpanded = false })
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.saveProfile() },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PitchBlack),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PitchBlack)
                            else Text("Save Changes", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            item { SettingsRow("Manage Categories", onClick = onManageCategories) }
            item { SettingsRow("Change Password", onClick = { showPasswordDialog = true }) }

            item {
                Button(
                    onClick = { showLogoutConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Zinc950, contentColor = Color.Red),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out")
                }
            }
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(viewModel, onDismiss = { showPasswordDialog = false })
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            containerColor = Zinc950,
            titleContentColor = PureWhite,
            textContentColor = Zinc400,
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your ledger.") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.logout(onLoggedOut) }) { Text("Log Out", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel", color = Zinc500) } }
        )
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Zinc950, RoundedCornerShape(16.dp))
            .border(1.dp, Zinc800, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = PureWhite)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Zinc500)
    }
}

@Composable
private fun ChangePasswordDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    val oldPassword by viewModel.oldPassword.collectAsState()
    val newPassword by viewModel.newPassword.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val error by viewModel.passwordError.collectAsState()
    val successMessage by viewModel.passwordSuccessMessage.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    AlertDialog(
        onDismissRequest = { viewModel.clearPasswordSuccessMessage(); onDismiss() },
        containerColor = Zinc950,
        titleContentColor = PureWhite,
        textContentColor = Zinc400,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (successMessage != null) {
                    Text(text = successMessage!!, color = Zinc400, fontSize = 13.sp)
                } else {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = viewModel::onOldPasswordChanged,
                        label = { Text("Current Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = viewModel::onNewPasswordChanged,
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChanged,
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error != null) {
                        Text(text = error!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (successMessage != null) {
                TextButton(onClick = { viewModel.clearPasswordSuccessMessage(); onDismiss() }) { Text("Done", color = PureWhite) }
            } else {
                TextButton(onClick = { viewModel.submitPasswordChange() }, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PureWhite)
                    else Text("Update", color = PureWhite)
                }
            }
        },
        dismissButton = {
            if (successMessage == null) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = Zinc500) }
            }
        }
    )
}
