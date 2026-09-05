package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvestmentEntity
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.components.ContactSupportSection
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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Independent limits per currency
private const val USDT_MIN = 50.0
private const val USDT_MAX = 5000.0
private const val PHP_MIN = 3000.0
private const val PHP_MAX = 100000.0

@Composable
fun InvestScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSubmitting by dashboardViewModel.isSubmittingInvestment.collectAsState()
    val errorMessage by dashboardViewModel.investmentErrorMessage.collectAsState()

    val scrollState = rememberScrollState()

    // Default currency from user profile or USDT
    var selectedCurrency by remember(currentUser) {
        mutableStateOf(currentUser?.selectedCurrency?.ifBlank { "USDT" } ?: "USDT")
    }

    var selectedNetwork by remember { mutableStateOf("TRC20") } // TRC20 or BEP20 for USDT
    var selectedDurationHours by remember { mutableIntStateOf(24) } // 24 or 48 hours
    var amountInput by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var createdInvestment by remember { mutableStateOf<InvestmentEntity?>(null) }
    var termsAccepted by remember { mutableStateOf(false) }

    // Redirect to login if user is not authenticated
    if (currentUser == null) {
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
                        contentDescription = "Authentication Required",
                        tint = SleekPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Authentication Required",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please log in with your investor credentials to create a new investment.",
                        fontSize = 14.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onNavigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
                    ) {
                        Text("Log In Now")
                    }
                }
            }
        }
        return
    }

    val user = currentUser!!

    // Validation computations
    val minLimit = if (selectedCurrency == "USDT") USDT_MIN else PHP_MIN
    val maxLimit = if (selectedCurrency == "USDT") USDT_MAX else PHP_MAX

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountEmpty = amountInput.isBlank()
    val isBelowMin = !isAmountEmpty && parsedAmount < minLimit
    val isAboveMax = !isAmountEmpty && parsedAmount > maxLimit
    val isAmountValid = !isAmountEmpty && parsedAmount in minLimit..maxLimit

    val currencySymbol = if (selectedCurrency == "USDT") "USDT" else "₱"
    val currencyFormatter = remember(selectedCurrency) {
        if (selectedCurrency == "PHP") {
            NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        } else {
            val nf = NumberFormat.getNumberInstance(Locale.US)
            nf.minimumFractionDigits = 2
            nf.maximumFractionDigits = 2
            nf
        }
    }

    val quickPresets = if (selectedCurrency == "USDT") {
        listOf(50.0, 100.0, 500.0, 1000.0, 5000.0)
    } else {
        listOf(3000.0, 5000.0, 10000.0, 50000.0, 100000.0)
    }

    // Expected timestamps
    val currentTimeMillis = System.currentTimeMillis()
    val expectedEndMillis = currentTimeMillis + (selectedDurationHours * 3600L * 1000L)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigate("dashboard") },
                modifier = Modifier.testTag("invest_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Dashboard",
                    tint = SleekTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "New Investment Allocation",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )
                Text(
                    text = "Secure institutional portfolio management",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Investment Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .testTag("invest_form_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurface),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Section 1: Currency Selection
                Text(
                    text = "1. SELECT CURRENCY & ASSET CLASS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // USDT Option
                    CurrencySelectOption(
                        title = "USDT (Tether)",
                        subtitle = "50 – 5,000 USDT",
                        networkInfo = "TRC20 / BEP20 Networks",
                        isSelected = selectedCurrency == "USDT",
                        onClick = {
                            selectedCurrency = "USDT"
                            amountInput = ""
                            dashboardViewModel.clearInvestmentError()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "currency_select_usdt"
                    )

                    // PHP Option
                    CurrencySelectOption(
                        title = "PHP (Philippine Peso)",
                        subtitle = "₱3,000 – ₱100,000",
                        networkInfo = "GCash / Maya / Bank",
                        isSelected = selectedCurrency == "PHP",
                        onClick = {
                            selectedCurrency = "PHP"
                            amountInput = ""
                            dashboardViewModel.clearInvestmentError()
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "currency_select_php"
                    )
                }

                // If USDT is selected, show network selection
                if (selectedCurrency == "USDT") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Transfer Network:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TRC20" to "TRON Network (TRC20)", "BEP20" to "BNB Smart Chain (BEP20)").forEach { (net, label) ->
                            val isNetSelected = selectedNetwork == net
                            Surface(
                                onClick = { selectedNetwork = net },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isNetSelected) SleekPrimaryContainer else SleekSurfaceVariant,
                                border = BorderStroke(1.dp, if (isNetSelected) SleekPrimary else SleekCardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isNetSelected) Icons.Default.CheckCircle else Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = if (isNetSelected) SleekPrimary else SleekTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isNetSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isNetSelected) SleekOnPrimaryContainer else SleekTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SleekCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Investment Duration (24h - 48h)
                Text(
                    text = "2. SELECT INVESTMENT DURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configurable yield cycle with dynamic institutional portfolio management",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DurationSelectOption(
                        hours = 24,
                        label = "24 Hours (1 Day)",
                        detail = "Short-cycle tactical position",
                        isSelected = selectedDurationHours == 24,
                        onClick = { selectedDurationHours = 24 },
                        modifier = Modifier.weight(1f),
                        testTag = "duration_select_24"
                    )

                    DurationSelectOption(
                        hours = 48,
                        label = "48 Hours (2 Days)",
                        detail = "Extended strategic allocation",
                        isSelected = selectedDurationHours == 48,
                        onClick = { selectedDurationHours = 48 },
                        modifier = Modifier.weight(1f),
                        testTag = "duration_select_48"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SleekCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Amount Entry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. ENTER INVESTMENT AMOUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary,
                        letterSpacing = 1.sp
                    )

                    // Independent limits pill
                    Surface(
                        color = SleekSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Text(
                            text = if (selectedCurrency == "USDT") "Limit: 50 – 5,000 USDT" else "Limit: ₱3,000 – ₱100,000",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { input ->
                        // Only allow digits and at most one decimal point
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountInput = input
                            dashboardViewModel.clearInvestmentError()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("investment_amount_input"),
                    placeholder = {
                        Text(
                            text = if (selectedCurrency == "USDT") "e.g. 500" else "e.g. 10000",
                            color = SleekTextMuted
                        )
                    },
                    leadingIcon = {
                        Text(
                            text = if (selectedCurrency == "USDT") "USDT" else "₱",
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    trailingIcon = {
                        if (isAmountValid) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Valid Amount",
                                tint = Color(0xFF10B981)
                            )
                        } else if (!isAmountEmpty) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Invalid Amount",
                                tint = SleekError
                            )
                        }
                    },
                    isError = isBelowMin || isAboveMax,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekSurfaceVariant,
                        unfocusedContainerColor = SleekSurfaceVariant,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekCardBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Inline validation feedback
                if (isBelowMin) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedCurrency == "USDT") "Amount is below minimum limit of 50 USDT." else "Amount is below minimum limit of ₱3,000.",
                        fontSize = 11.sp,
                        color = SleekError,
                        fontWeight = FontWeight.Medium
                    )
                } else if (isAboveMax) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedCurrency == "USDT") "Amount exceeds maximum limit of 5,000 USDT." else "Amount exceeds maximum limit of ₱100,000.",
                        fontSize = 11.sp,
                        color = SleekError,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick preset chips
                Text(
                    text = "Quick Presets:",
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { preset ->
                        val presetLabel = if (selectedCurrency == "USDT") {
                            "${preset.toInt()} USDT"
                        } else {
                            "₱${NumberFormat.getNumberInstance(Locale.US).format(preset.toInt())}"
                        }
                        Surface(
                            onClick = {
                                amountInput = if (preset % 1.0 == 0.0) preset.toInt().toString() else preset.toString()
                                dashboardViewModel.clearInvestmentError()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = SleekSurfaceVariant,
                            border = BorderStroke(1.dp, if (amountInput == preset.toInt().toString()) SleekPrimary else SleekCardBorder)
                        ) {
                            Text(
                                text = presetLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (amountInput == preset.toInt().toString()) SleekPrimary else SleekTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = SleekCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Investment Plan Summary & Schedule Preview
                Text(
                    text = "4. ALLOCATION SCHEDULE & PARAMETERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = SleekSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SleekCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ScheduleRow(
                            label = "Asset Class & Currency",
                            value = if (selectedCurrency == "USDT") "USDT ($selectedNetwork)" else "PHP (Philippine Peso)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScheduleRow(
                            label = "Investment Principal",
                            value = if (isAmountValid) {
                                if (selectedCurrency == "USDT") "$amountInput USDT" else "₱${NumberFormat.getNumberInstance(Locale.US).format(parsedAmount)}"
                            } else "—",
                            isHighlight = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScheduleRow(
                            label = "Duration Cycle",
                            value = "$selectedDurationHours Hours (${if (selectedDurationHours == 24) "1 Day" else "2 Days"})"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScheduleRow(
                            label = "Expected Maturity Date",
                            value = dateFormat.format(Date(expectedEndMillis))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ScheduleRow(
                            label = "Initial Performance",
                            value = "0.00% (Variable Realized Yield)"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Risk and Variable Performance Notice (No Guaranteed Multiple Promise)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant.copy(alpha = 0.6f)),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Risk Notice",
                            tint = SleekPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CPI enforces variable performance based on authentic capital market operations. No guaranteed multiples (e.g. 5x or 10x) are promised. Performance is recorded upon verified yield reconciliation.",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = SleekErrorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = SleekError,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                color = SleekError,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Review and Invest CTA Button
                Button(
                    onClick = {
                        if (isAmountValid) {
                            showConfirmationDialog = true
                        }
                    },
                    enabled = isAmountValid && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("invest_submit_review_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        disabledContainerColor = SleekSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Securing Allocation...",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REVIEW & ALLOCATE INVESTMENT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        ContactSupportSection()
    }

    // Step 3 Confirmation Modal before database insertion
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) showConfirmationDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Confirm Investment Allocation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Please review your investment details and risk terms before confirming creation:",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SleekSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            ScheduleRow(label = "Investor", value = user.fullName)
                            Spacer(modifier = Modifier.height(6.dp))
                            ScheduleRow(label = "Selected Currency", value = "$selectedCurrency ${if (selectedCurrency == "USDT") "($selectedNetwork)" else ""}")
                            Spacer(modifier = Modifier.height(6.dp))
                            ScheduleRow(
                                label = "Allocated Amount",
                                value = if (selectedCurrency == "USDT") "$amountInput USDT" else "₱${NumberFormat.getNumberInstance(Locale.US).format(parsedAmount)}",
                                isHighlight = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ScheduleRow(label = "Duration", value = "$selectedDurationHours Hours")
                            Spacer(modifier = Modifier.height(6.dp))
                            ScheduleRow(label = "Start Timestamp", value = dateFormat.format(Date(currentTimeMillis)))
                            Spacer(modifier = Modifier.height(6.dp))
                            ScheduleRow(label = "Expected Maturity", value = dateFormat.format(Date(expectedEndMillis)))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Risk Disclosure & Terms",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By clicking confirm, you authorize the allocation of $amountInput $selectedCurrency into a $selectedDurationHours-hour CPI managed portfolio. Initial performance is recorded as 0.00% and updated subject to authentic audited results.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        dashboardViewModel.submitInvestment(
                            userId = user.id,
                            amount = parsedAmount,
                            currency = selectedCurrency,
                            durationHours = selectedDurationHours,
                            network = if (selectedCurrency == "USDT") selectedNetwork else "FIAT",
                            onSuccess = { investment ->
                                showConfirmationDialog = false
                                createdInvestment = investment
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    modifier = Modifier.testTag("confirm_investment_modal_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = "Confirm & Allocate",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmationDialog = false },
                    enabled = !isSubmitting
                ) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Success Dialog on creation
    if (createdInvestment != null) {
        val inv = createdInvestment!!
        AlertDialog(
            onDismissRequest = {
                createdInvestment = null
                onNavigate("investments")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Investment Successfully Allocated",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
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
                        text = "Position ID: ${inv.id}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your investment of ${inv.amount} ${inv.currency} (${inv.durationHours} Hours) has been registered in the database and logged to your transaction ledger.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        createdInvestment = null
                        onNavigate("investments")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    modifier = Modifier.testTag("view_investments_button")
                ) {
                    Text("View My Investments", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        createdInvestment = null
                        onNavigate("dashboard")
                    }
                ) {
                    Text("Go to Dashboard", color = SleekTextSecondary)
                }
            },
            containerColor = SleekSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CurrencySelectOption(
    title: String,
    subtitle: String,
    networkInfo: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SleekPrimaryContainer else SleekSurfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) SleekPrimary else SleekCardBorder
        ),
        modifier = modifier.testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SleekOnPrimaryContainer else SleekTextPrimary
                )
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = if (isSelected) SleekPrimary else SleekTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) SleekPrimary else SleekTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = networkInfo,
                fontSize = 10.sp,
                color = SleekTextMuted
            )
        }
    }
}

@Composable
private fun DurationSelectOption(
    hours: Int,
    label: String,
    detail: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SleekPrimaryContainer else SleekSurfaceVariant,
        border = BorderStroke(
            1.5.dp,
            if (isSelected) SleekPrimary else SleekCardBorder
        ),
        modifier = modifier.testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SleekOnPrimaryContainer else SleekTextPrimary
                )
                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isSelected) SleekPrimary else SleekTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                fontSize = 11.sp,
                color = SleekTextSecondary
            )
        }
    }
}

@Composable
private fun ScheduleRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SleekTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) SleekPrimary else SleekTextPrimary
        )
    }
}
