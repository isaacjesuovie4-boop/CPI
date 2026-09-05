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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthViewModel
import com.example.ui.UiState
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekErrorContainer
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryBorder
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val profileUpdateState by authViewModel.profileUpdateState.collectAsState()
    val scrollState = rememberScrollState()

    if (currentUser == null) {
        // Redirection for unauthenticated access
        LaunchedEffect(Unit) {
            onNavigate("login")
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SleekBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Authentication Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Redirecting to login portal...",
                        fontSize = 13.sp,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onNavigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Go to Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val user = currentUser!!

    var fullName by remember(user) { mutableStateOf(user.fullName) }
    var phoneNumber by remember(user) { mutableStateOf(user.phoneNumber) }
    var country by remember(user) { mutableStateOf(user.country) }
    var occupation by remember(user) { mutableStateOf(user.occupation) }
    var localError by remember { mutableStateOf<String?>(null) }

    val dateFormatted = remember(user.createdAt) {
        val sdf = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(user.createdAt))
    }

    LaunchedEffect(Unit) {
        authViewModel.resetProfileUpdateState()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigate("dashboard") },
                modifier = Modifier.testTag("profile_back_to_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Dashboard",
                    tint = SleekTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Account Profile & Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Text(
                    text = "Manage your verified investor identity details",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Avatar & Key Stats Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_header_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SleekPrimaryContainer)
                            .border(1.5.dp, SleekPrimaryBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user.email,
                            fontSize = 13.sp,
                            color = SleekTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SleekPrimaryContainer)
                            .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = user.accountStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SleekCardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "ACCOUNT ID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted
                        )
                        Text(
                            text = user.id,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                    }

                    Column {
                        Text(
                            text = "BASE CURRENCY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted
                        )
                        Text(
                            text = user.selectedCurrency,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }

                    Column {
                        Text(
                            text = "MEMBER SINCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted
                        )
                        Text(
                            text = dateFormatted.split("•").firstOrNull()?.trim() ?: dateFormatted,
                            fontSize = 12.sp,
                            color = SleekTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Update Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Personal Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Text(
                    text = "Update your identity and contact parameters",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Success message
                AnimatedVisibility(visible = profileUpdateState is UiState.Success) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekPrimaryContainer)
                            .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Profile information updated successfully.",
                                color = SleekOnPrimaryContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Error message
                AnimatedVisibility(visible = profileUpdateState is UiState.Error || localError != null) {
                    val msg = when (val s = profileUpdateState) {
                        is UiState.Error -> s.message
                        else -> localError ?: ""
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekErrorContainer)
                            .border(1.dp, SleekError.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = SleekError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = SleekError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Full Name
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
                        localError = null
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SleekPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_input_fullname")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Number
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
                        localError = null
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = SleekPrimary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_input_phone")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Country
                Text(
                    text = "Country of Residence *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = country,
                    onValueChange = {
                        country = it
                        localError = null
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Public, contentDescription = null, tint = SleekPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_input_country")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Occupation
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
                        localError = null
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Work, contentDescription = null, tint = SleekPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = customTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_input_occupation")
                )

                Spacer(modifier = Modifier.height(20.dp))

                val isUpdating = profileUpdateState is UiState.Loading
                Button(
                    onClick = {
                        when {
                            fullName.isBlank() -> localError = "Please enter your full name."
                            phoneNumber.isBlank() -> localError = "Please enter your phone number."
                            country.isBlank() -> localError = "Please enter your country."
                            occupation.isBlank() -> localError = "Please enter your occupation."
                            else -> {
                                localError = null
                                authViewModel.updateProfile(
                                    userId = user.id,
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    country = country,
                                    occupation = occupation
                                )
                            }
                        }
                    },
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_save_button")
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SAVE CHANGES",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Immutable & Security Protected Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_security_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security & Auditable Settings (Read-Only)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "In compliance with CPI financial audit regulations, base currency, financial ledger balances, investment portfolios, and transaction records cannot be modified directly.",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                ReadOnlyInfoField(
                    label = "Registered Email (Account Key)",
                    value = user.email,
                    icon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(10.dp))

                ReadOnlyInfoField(
                    label = "Base Currency Selection",
                    value = if (user.selectedCurrency == "USDT") "USDT (Tether USD) • Networks: TRC20, BEP20" else "PHP (Philippine Peso) • Domestic Gateway",
                    icon = Icons.Default.CurrencyExchange
                )

                Spacer(modifier = Modifier.height(10.dp))

                ReadOnlyInfoField(
                    label = "Account Registration Timestamp",
                    value = dateFormatted,
                    icon = Icons.Default.Info
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Return to Dashboard Button
        OutlinedButton(
            onClick = { onNavigate("dashboard") },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, SleekCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("profile_return_dashboard_button")
        ) {
            Text("RETURN TO DASHBOARD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ReadOnlyInfoField(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SleekSurface)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SleekTextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted
                )
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(SleekSurfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LOCKED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted
                )
            }
        }
    }
}
