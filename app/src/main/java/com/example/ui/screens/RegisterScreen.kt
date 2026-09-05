package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthViewModel
import com.example.ui.UiState
import com.example.ui.components.CpiLogo
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekErrorContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryBorder
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("United States") }
    var occupation by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USDT") } // "USDT" or "PHP"
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel.registerState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            onNavigate("dashboard")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .testTag("register_screen")
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CpiLogo(iconSize = 42.dp, isDark = false)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Investor Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Register with Crest Point Investment to establish your portfolio foundation.",
                fontSize = 13.sp,
                color = SleekTextSecondary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Server / Local Error Display
                AnimatedVisibility(visible = registerState is UiState.Error || localErrorMessage != null) {
                    val errorText = when (val state = registerState) {
                        is UiState.Error -> state.message
                        else -> localErrorMessage ?: ""
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekErrorContainer)
                            .border(1.dp, SleekError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = SleekError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorText,
                                color = SleekError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 1. Full Name
                Text(
                    text = "Full Name *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("e.g. Alexander Mitchell", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SleekPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fullname")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Email Address
                Text(
                    text = "Email Address *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("e.g. alex@example.com", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = SleekPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_email")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Phone Number
                Text(
                    text = "Phone Number *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("e.g. +1 555 123 4567", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = SleekPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Country Selector / Input
                Text(
                    text = "Country of Residence *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                CountryDropdownSelector(
                    selectedCountry = country,
                    onCountrySelected = {
                        country = it
                        localErrorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Occupation
                Text(
                    text = "Occupation *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = occupation,
                    onValueChange = {
                        occupation = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("e.g. Financial Analyst / Business Owner", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Work, contentDescription = null, tint = SleekPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_occupation")
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Currency Selector (USDT vs PHP)
                Text(
                    text = "Selected Currency *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your base funding currency. Limits are separate and strictly non-convertible.",
                    fontSize = 11.sp,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                CurrencySelectionRadioGroup(
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = {
                        selectedCurrency = it
                        localErrorMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 7. Password
                Text(
                    text = "Password * (Min 6 characters)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("Enter secure password", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = SleekPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = SleekTextMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_password")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 8. Confirm Password
                Text(
                    text = "Confirm Password *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        localErrorMessage = null
                    },
                    placeholder = { Text("Confirm your password", color = SleekTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = SleekPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = SleekTextMuted
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_confirm_password")
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Register Button
                val isLoading = registerState is UiState.Loading
                Button(
                    onClick = {
                        // Client-side quick checks
                        when {
                            fullName.isBlank() -> localErrorMessage = "Please enter your full name."
                            email.isBlank() || !email.contains("@") -> localErrorMessage = "Please enter a valid email address."
                            phoneNumber.isBlank() -> localErrorMessage = "Please enter your phone number."
                            country.isBlank() -> localErrorMessage = "Please select your country."
                            occupation.isBlank() -> localErrorMessage = "Please enter your occupation."
                            password.length < 6 -> localErrorMessage = "Password must be at least 6 characters."
                            password != confirmPassword -> localErrorMessage = "Passwords do not match."
                            else -> {
                                localErrorMessage = null
                                authViewModel.register(
                                    fullName = fullName,
                                    email = email,
                                    phoneNumber = phoneNumber,
                                    country = country,
                                    occupation = occupation,
                                    selectedCurrency = selectedCurrency,
                                    password = password,
                                    confirmPassword = confirmPassword
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_registration_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "REGISTER HERE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Link to Login
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 13.sp,
                        color = SleekTextSecondary
                    )
                    Text(
                        text = "LOGIN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary,
                        modifier = Modifier
                            .clickable { onNavigate("login") }
                            .padding(4.dp)
                            .testTag("link_to_login")
                    )
                }
            }
        }
    }
}

@Composable
fun CurrencySelectionRadioGroup(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("currency_selector_group")
    ) {
        // USDT Option
        val isUsdt = selectedCurrency == "USDT"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onCurrencySelected("USDT") }
                .testTag("currency_option_usdt"),
            colors = CardDefaults.cardColors(
                containerColor = if (isUsdt) SleekPrimaryContainer else SleekSurface
            ),
            border = BorderStroke(
                1.dp,
                if (isUsdt) SleekPrimary else SleekCardBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isUsdt,
                    onClick = { onCurrencySelected("USDT") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = SleekPrimary,
                        unselectedColor = SleekTextMuted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "USDT (Tether)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SleekPrimaryContainer)
                                .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TRC20 & BEP20",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Limits: 50 USDT – 5,000 USDT",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SleekPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // PHP Option
        val isPhp = selectedCurrency == "PHP"
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onCurrencySelected("PHP") }
                .testTag("currency_option_php"),
            colors = CardDefaults.cardColors(
                containerColor = if (isPhp) SleekPrimaryContainer else SleekSurface
            ),
            border = BorderStroke(
                1.dp,
                if (isPhp) SleekPrimary else SleekCardBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isPhp,
                    onClick = { onCurrencySelected("PHP") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = SleekPrimary,
                        unselectedColor = SleekTextMuted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PHP (Philippine Peso)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SleekTextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SleekPrimaryContainer)
                                .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Fiat Settlement",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Limits: ₱3,000 – ₱100,000",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SleekPrimary
                    )
                }
            }
        }

        // Mandatory Dynamic Display requirement box
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SleekSurfaceVariant)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedCurrency == "USDT") {
                        "Active Selected Limit: 50 USDT – 5,000 USDT"
                    } else {
                        "Active Selected Limit: ₱3,000 – ₱100,000"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
            }
        }
    }
}

@Composable
fun CountryDropdownSelector(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCountry,
            onValueChange = onCountrySelected,
            placeholder = { Text("Select or enter country", color = SleekTextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Public, contentDescription = null, tint = SleekPrimary)
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = customTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_country")
        )
    }
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SleekSurface,
    unfocusedContainerColor = SleekSurface,
    focusedBorderColor = SleekPrimary,
    unfocusedBorderColor = SleekCardBorder,
    focusedTextColor = SleekTextPrimary,
    unfocusedTextColor = SleekTextPrimary,
    cursorColor = SleekPrimary
)

