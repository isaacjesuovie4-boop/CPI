package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WithdrawalEntity
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.components.CpiLogo
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WithdrawScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val overview by dashboardViewModel.overview.collectAsState()
    val isSubmitting by dashboardViewModel.isSubmittingWithdrawal.collectAsState()
    val errorMessage by dashboardViewModel.withdrawalErrorMessage.collectAsState()
    val withdrawalSuccess by dashboardViewModel.withdrawalSuccess.collectAsState()
    val isOtpRequested by dashboardViewModel.isOtpRequested.collectAsState()
    val otpExpiresAt by dashboardViewModel.otpExpiresAt.collectAsState()

    val scrollState = rememberScrollState()

    var selectedCurrency by remember {
        mutableStateOf(currentUser?.selectedCurrency ?: "USDT")
    }

    var availableBalance by remember { mutableStateOf(0.0) }
    var amountInput by remember { mutableStateOf("") }

    // USDT specific
    var usdtNetwork by remember { mutableStateOf("TRC20") } // TRC20 or BEP20
    var walletAddressInput by remember { mutableStateOf("") }

    // PHP specific
    var phpPaymentMethod by remember { mutableStateOf("GCash") } // GCash, Maya, BDO, UnionBank, BPI
    var accountNameInput by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var accountNumberInput by remember { mutableStateOf("") }

    // Verification Code
    var otpCodeInput by remember { mutableStateOf("") }
    var showOtpPromptDialog by remember { mutableStateOf(false) }

    // Refresh available balance on currency change or user change
    LaunchedEffect(currentUser, selectedCurrency, overview) {
        currentUser?.let { user ->
            dashboardViewModel.fetchAvailableBalance(user.id, selectedCurrency) { bal ->
                availableBalance = bal
            }
        }
    }

    // Clear status on enter
    LaunchedEffect(Unit) {
        dashboardViewModel.clearWithdrawalStatus()
    }

    val user = currentUser
    if (user == null) {
        LaunchedEffect(Unit) {
            onNavigate("login")
        }
        return
    }

    val minAmount = if (selectedCurrency == "USDT") 50.0 else 3000.0
    val maxLimit = if (selectedCurrency == "USDT") 5000.0 else 100000.0

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountValid = parsedAmount >= minAmount && parsedAmount <= availableBalance

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigate("dashboard") },
                        modifier = Modifier.testTag("withdraw_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = SleekTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Capital Withdrawal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Secure Settlement & Payout Gateway",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                TextButton(
                    onClick = { onNavigate("withdrawals") },
                    modifier = Modifier.testTag("withdraw_view_history_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "History",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Currency Switcher Tabs (USDT vs PHP)
        TabRow(
            selectedTabIndex = if (selectedCurrency == "USDT") 0 else 1,
            containerColor = SleekSurface,
            contentColor = SleekPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[if (selectedCurrency == "USDT") 0 else 1]),
                    color = SleekPrimary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedCurrency == "USDT",
                onClick = {
                    selectedCurrency = "USDT"
                    amountInput = ""
                },
                text = {
                    Text(
                        text = "USDT (Crypto)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedCurrency == "USDT") SleekPrimary else SleekTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedCurrency == "PHP",
                onClick = {
                    selectedCurrency = "PHP"
                    amountInput = ""
                },
                text = {
                    Text(
                        text = "PHP (Philippine Peso)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (selectedCurrency == "PHP") SleekPrimary else SleekTextSecondary
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Available Balance Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_available_balance_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
            border = BorderStroke(1.dp, SleekPrimaryBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SleekPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REAL AVAILABLE BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnPrimaryContainer,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Verified & Unallocated Funds",
                                fontSize = 11.sp,
                                color = SleekTextSecondary
                            )
                        }
                    }

                    Surface(
                        color = SleekSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Text(
                            text = selectedCurrency,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (selectedCurrency == "USDT") {
                        "${String.format(Locale.US, "%,.2f", availableBalance)} USDT"
                    } else {
                        "₱${String.format(Locale.US, "%,.2f", availableBalance)}"
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SleekPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "A $selectedCurrency balance can only be withdrawn as $selectedCurrency. Withdrawal requests reserve your balance immediately to prevent double spending.",
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Withdrawal Details Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "WITHDRAWAL DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Amount Input
                Text(
                    text = "Withdrawal Amount (${selectedCurrency})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            amountInput = input
                        }
                    },
                    placeholder = {
                        Text(
                            text = if (selectedCurrency == "USDT") "Min: 50.00 USDT" else "Min: ₱3,000.00",
                            color = SleekTextMuted
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedContainerColor = SleekSurfaceVariant,
                        unfocusedContainerColor = SleekSurfaceVariant
                    ),
                    trailingIcon = {
                        Text(
                            text = selectedCurrency,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Percentage Chips (25%, 50%, 75%, Max)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.25 to "25%", 0.50 to "50%", 0.75 to "75%", 1.00 to "100% (Max)").forEach { (pct, label) ->
                        OutlinedButton(
                            onClick = {
                                val calc = (availableBalance * pct)
                                val rounded = (calc * 100.0).toLong() / 100.0
                                amountInput = if (rounded > 0) rounded.toString() else "0"
                            },
                            enabled = availableBalance > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, SleekCardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SleekPrimary
                            )
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Destination Details according to Currency
                if (selectedCurrency == "USDT") {
                    // USDT Network Selector (TRC20 vs BEP20)
                    Text(
                        text = "Transfer Network",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("TRC20" to "TRON (TRC20)", "BEP20" to "BNB Chain (BEP20)").forEach { (net, label) ->
                            FilterChip(
                                selected = usdtNetwork == net,
                                onClick = { usdtNetwork = net },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimary,
                                    selectedLabelColor = Color(0xFF0F172A),
                                    containerColor = SleekSurfaceVariant,
                                    labelColor = SleekTextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Receiving USDT ($usdtNetwork) Wallet Address",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = walletAddressInput,
                        onValueChange = { walletAddressInput = it.trim() },
                        placeholder = {
                            Text(
                                text = if (usdtNetwork == "TRC20") "Enter your receiving TRC20 wallet address" else "Enter your receiving BEP20 wallet address (0x...)",
                                color = SleekTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_wallet_address_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekSurfaceVariant,
                            unfocusedContainerColor = SleekSurfaceVariant
                        )
                    )
                } else {
                    // PHP Payment Method / Bank Selection
                    Text(
                        text = "Receiving Method / Bank",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("GCash", "Maya", "BDO", "UnionBank", "BPI").forEach { method ->
                            FilterChip(
                                selected = phpPaymentMethod == method,
                                onClick = { phpPaymentMethod = method },
                                label = { Text(method, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SleekPrimary,
                                    selectedLabelColor = Color(0xFF0F172A),
                                    containerColor = SleekSurfaceVariant,
                                    labelColor = SleekTextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Account Holder Full Name",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = accountNameInput,
                        onValueChange = { accountNameInput = it },
                        placeholder = { Text("Account Holder Name", color = SleekTextMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekSurfaceVariant,
                            unfocusedContainerColor = SleekSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$phpPaymentMethod Account / Mobile Number",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = accountNumberInput,
                        onValueChange = { accountNumberInput = it.trim() },
                        placeholder = {
                            Text(
                                text = if (phpPaymentMethod == "GCash" || phpPaymentMethod == "Maya") "e.g. 0917-XXX-XXXX" else "e.g. Bank Account Number",
                                color = SleekTextMuted
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_account_number_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekSurfaceVariant,
                            unfocusedContainerColor = SleekSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Banner
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekErrorContainer),
                        border = BorderStroke(1.dp, SleekErrorBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SleekError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = SleekOnErrorContainer,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Compliance Notice Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Security Protocol: All withdrawal requests undergo cryptographic verification and multi-signature authorization. Dispatched to destination upon administrator audit.",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Withdrawal Request Button
                val destinationValid = if (selectedCurrency == "USDT") {
                    walletAddressInput.length >= 8
                } else {
                    accountNameInput.isNotBlank() && accountNumberInput.length >= 6
                }

                val canProceed = isAmountValid && destinationValid && !isSubmitting

                Button(
                    onClick = {
                        // Request OTP Verification Step
                        dashboardViewModel.requestWithdrawalOtp(user.id)
                        showOtpPromptDialog = true
                    },
                    enabled = canProceed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_withdrawal_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        disabledContainerColor = SleekSurfaceVariant,
                        contentColor = Color(0xFF0F172A),
                        disabledContentColor = SleekTextMuted
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Securing Ledger...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (canProceed) Color(0xFF0F172A) else SleekTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verify & Submit Withdrawal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Step 7: Cryptographic Withdrawal OTP Verification Dialog
    if (showOtpPromptDialog) {
        val destinationSummary = if (selectedCurrency == "USDT") {
            "$usdtNetwork • $walletAddressInput"
        } else {
            "$phpPaymentMethod • $accountNameInput ($accountNumberInput)"
        }

        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) showOtpPromptDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Withdrawal Authorization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "To authorize the withdrawal of ${parsedAmount} ${selectedCurrency} to ${destinationSummary}, enter the 6-digit cryptographic security code generated for your session.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Verification Code generated server-side.",
                                fontSize = 11.sp,
                                color = SleekPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Code valid for 5 minutes. Enter code to authorize:",
                                fontSize = 11.sp,
                                color = SleekTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = otpCodeInput,
                        onValueChange = { if (it.length <= 6) otpCodeInput = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("6-digit Code", color = SleekTextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdrawal_otp_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary,
                            unfocusedBorderColor = SleekCardBorder,
                            focusedContainerColor = SleekSurfaceVariant,
                            unfocusedContainerColor = SleekSurfaceVariant
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = SleekError,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val destinationStr = if (selectedCurrency == "USDT") walletAddressInput else "$accountNameInput - $accountNumberInput"
                        val networkStr = if (selectedCurrency == "USDT") usdtNetwork else phpPaymentMethod

                        dashboardViewModel.submitWithdrawalRequest(
                            userId = user.id,
                            amount = parsedAmount,
                            currency = selectedCurrency,
                            destination = destinationStr,
                            network = networkStr,
                            otpCode = otpCodeInput,
                            onSuccess = {
                                showOtpPromptDialog = false
                            }
                        )
                    },
                    enabled = otpCodeInput.length == 6 && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_otp_withdrawal_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Authorize Request", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOtpPromptDialog = false },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }

    // Success Confirmation Dialog
    if (withdrawalSuccess != null) {
        val wth = withdrawalSuccess!!
        AlertDialog(
            onDismissRequest = {
                dashboardViewModel.clearWithdrawalStatus()
                onNavigate("withdrawals")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SleekSuccess,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text(
                    text = "Withdrawal Submitted",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your withdrawal request has been placed in the verification queue.",
                        fontSize = 13.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Withdrawal ID:", fontSize = 11.sp, color = SleekTextSecondary)
                                Text(wth.withdrawalId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount:", fontSize = 11.sp, color = SleekTextSecondary)
                                Text("${wth.amount} ${wth.currency}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Destination:", fontSize = 11.sp, color = SleekTextSecondary)
                                Text("${wth.network} (${wth.destination.take(16)}...)", fontSize = 11.sp, color = SleekTextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Status:", fontSize = 11.sp, color = SleekTextSecondary)
                                Text(wth.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SleekWarning)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "The requested funds are now reserved. You can monitor the real-time status in your Withdrawal History.",
                        fontSize = 11.sp,
                        color = SleekTextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardViewModel.clearWithdrawalStatus()
                        onNavigate("withdrawals")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("View Withdrawal History", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dashboardViewModel.clearWithdrawalStatus()
                        onNavigate("dashboard")
                    }
                ) {
                    Text("Go to Dashboard", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface
        )
    }
}
