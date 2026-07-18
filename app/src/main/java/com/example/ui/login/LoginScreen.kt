package com.example.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc700
import com.example.ui.theme.Zinc800
import com.example.ui.theme.Zinc900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val name by viewModel.name.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isSignUpMode by viewModel.isSignUpMode.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    // Navigation trigger
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is LoginViewModel.LoginEvent.NavigateToDashboard -> onNavigateToDashboard()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Header / Brand
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Minimalist Logo Icon (Monochromatic Square Vector Icon)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Zinc900)
                        .border(1.dp, Zinc800, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = PureWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSignUpMode) "CREATE ACCOUNT" else "SECURE SIGN IN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = PureWhite,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isSignUpMode) "Join the ledger system" else "Enter your workspace to monitor balances",
                    fontSize = 13.sp,
                    color = Zinc400,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Input Fields
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SignUp Mode Only Fields (Name & Currency)
                AnimatedVisibility(
                    visible = isSignUpMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = viewModel::onNameChanged,
                            label = { Text("Display Name") },
                            placeholder = { Text("Jane Doe") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = PureWhite,
                                unfocusedLabelColor = Zinc400,
                                cursorColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = currency,
                            onValueChange = viewModel::onCurrencyChanged,
                            label = { Text("Reporting Currency") },
                            placeholder = { Text("USD, EUR, GBP...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = PureWhite,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = PureWhite,
                                unfocusedLabelColor = Zinc400,
                                cursorColor = PureWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("currency_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email Address") },
                    placeholder = { Text("user@example.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = Zinc800,
                        focusedLabelColor = PureWhite,
                        unfocusedLabelColor = Zinc400,
                        cursorColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Secret Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("password_toggle")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Zinc400
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = PureWhite,
                        unfocusedBorderColor = Zinc800,
                        focusedLabelColor = PureWhite,
                        unfocusedLabelColor = Zinc400,
                        cursorColor = PureWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Error Message Banner
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .testTag("error_banner")
                )
            }

            // CTA Button & Toggle Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = viewModel::onSubmit,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PureWhite,
                        contentColor = PitchBlack,
                        disabledContainerColor = Zinc800,
                        disabledContentColor = Zinc400
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = PitchBlack,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUpMode) "AUTHORIZE & REGISTER" else "AUTHORIZE CONNECT",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUpMode) "Already verified?" else "New ledger deployment?",
                        fontSize = 13.sp,
                        color = Zinc400
                    )
                    Text(
                        text = if (isSignUpMode) "Sign In" else "Sign Up",
                        fontSize = 13.sp,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.toggleMode() }
                            .testTag("toggle_mode_button")
                    )
                }
            }
        }
    }
}
