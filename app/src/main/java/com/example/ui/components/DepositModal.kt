package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.ui.DashboardViewModel
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy
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
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private const val USDT_DEPOSIT_MIN = 50.0
private const val USDT_DEPOSIT_MAX = 5000.0
private const val PHP_DEPOSIT_MIN = 3000.0
private const val PHP_DEPOSIT_MAX = 100000.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositModal(
    user: UserEntity,
    dashboardViewModel: DashboardViewModel,
    onDismiss: () -> Unit
) {
    var selectedCurrency by remember { mutableStateOf(user.selectedCurrency) }
    val publishedAccounts by dashboardViewModel.publishedPaymentAccounts.collectAsState()
    val isSubmitting by dashboardViewModel.isSubmittingDeposit.collectAsState()
    val errorMessage by dashboardViewModel.depositErrorMessage.collectAsState()
    val successTx by dashboardViewModel.depositSuccessTransaction.collectAsState()

    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var referenceInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current
    var copiedField by remember { mutableStateOf<String?>(null) }

    // Live clock for 30-minute visibility timer
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        dashboardViewModel.setSelectedCurrency(selectedCurrency)
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(selectedCurrency) {
        dashboardViewModel.setSelectedCurrency(selectedCurrency)
        selectedAccountId = null
    }

    // Filter active & valid published accounts for the selected currency
    val validAccountsForCurrency = remember(publishedAccounts, currentTimeMs, selectedCurrency) {
        publishedAccounts.filter { it.currency == selectedCurrency && it.isActive && it.isCurrentlyValid(currentTimeMs) }
    }

    // Auto-select the first available valid account
    LaunchedEffect(validAccountsForCurrency) {
        if (selectedAccountId == null || validAccountsForCurrency.none { it.id == selectedAccountId }) {
            selectedAccountId = validAccountsForCurrency.firstOrNull()?.id
        }
    }

    val selectedAccount = remember(selectedAccountId, publishedAccounts) {
        publishedAccounts.firstOrNull { it.id == selectedAccountId }
    }

    val isAccountValid = selectedAccount?.isCurrentlyValid(currentTimeMs) == true
    val remainingSec = selectedAccount?.getRemainingSeconds(currentTimeMs) ?: 0L
    val remainingFormatted = remember(remainingSec) {
        val m = remainingSec / 60
        val s = remainingSec % 60
        "%02d:%02d".format(m, s)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) {
                dashboardViewModel.clearDepositStatus()
                onDismiss()
            }
        },
        containerColor = Color(0xFF0F172A),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("investor_deposit_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = CpiGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official Deposit Gateway",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        dashboardViewModel.clearDepositStatus()
                        onDismiss()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (successTx != null) {
                    // Success View
                    DepositSuccessReceipt(
                        tx = successTx!!,
                        onDone = {
                            dashboardViewModel.clearDepositStatus()
                            onDismiss()
                        }
                    )
                } else {
                    // Currency Selector Tabs
                    Text(
                        text = "SELECT DEPOSIT CURRENCY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("USDT", "PHP").forEach { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = {
                                    selectedCurrency = curr
                                    amountInput = ""
                                    localValidationError = null
                                },
                                label = {
                                    Text(
                                        text = if (curr == "USDT") "USDT (Crypto)" else "PHP (Philippine Peso)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CpiGold,
                                    selectedLabelColor = CpiNavy,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("deposit_chip_currency_$curr")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Account Selection (if multiple methods available)
                    if (validAccountsForCurrency.size > 1) {
                        Text(
                            text = "RECEIVING PAYMENT CHANNEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            validAccountsForCurrency.forEach { acc ->
                                val isSelected = acc.id == selectedAccountId
                                Surface(
                                    onClick = { selectedAccountId = acc.id },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0B1322),
                                    border = BorderStroke(1.dp, if (isSelected) CpiGold else Color(0xFF334155)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = acc.paymentMethod,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CpiGold else Color.White
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Published Details Card OR Expired / Unavailable Warning
                    if (selectedAccount == null || !isAccountValid) {
                        // EXPIRED / NO PUBLISHED ACCOUNTS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_account_expired_banner")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Payment Details Expired or Unavailable",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "These payment details have expired. Please request new payment details or contact compliance support.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        // PUBLISHED DETAILS DISPLAY WITH LIVE 30-MIN COUNTDOWN
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("published_payment_details_card")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // 30-Minute Live Countdown Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = Color(0xFF34D399),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Payment details valid for:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFCBD5E1)
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, Color(0xFF10B981))
                                    ) {
                                        Text(
                                            text = remainingFormatted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(10.dp))

                                if (selectedAccount.currency == "PHP") {
                                    // PHP Details Display
                                    PaymentDetailRow(
                                        label = "Payment Method:",
                                        value = selectedAccount.paymentMethod
                                    )

                                    if (!selectedAccount.accountName.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        PaymentDetailRow(
                                            label = "Account Name:",
                                            value = selectedAccount.accountName
                                        )
                                    }

                                    if (!selectedAccount.accountNumber.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        CopyablePaymentRow(
                                            label = "Account Number:",
                                            value = selectedAccount.accountNumber,
                                            isCopied = copiedField == "account_number",
                                            onCopy = {
                                                clipboardManager.setText(AnnotatedString(selectedAccount.accountNumber))
                                                copiedField = "account_number"
                                            }
                                        )
                                    }
                                } else {
                                    // USDT Details Display
                                    if (!selectedAccount.network.isNullOrBlank()) {
                                        PaymentDetailRow(
                                            label = "Network:",
                                            value = "${selectedAccount.network} (Strictly supported)"
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    if (!selectedAccount.walletAddress.isNullOrBlank()) {
                                        CopyablePaymentRow(
                                            label = "Official Wallet Address:",
                                            value = selectedAccount.walletAddress,
                                            isCopied = copiedField == "wallet_address",
                                            onCopy = {
                                                clipboardManager.setText(AnnotatedString(selectedAccount.walletAddress))
                                                copiedField = "wallet_address"
                                            }
                                        )
                                    }
                                }

                                if (selectedAccount.instructions.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFF0F172A),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Instructions: ${selectedAccount.instructions}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Deposit Submission Form
                        Text(
                            text = "ENTER DEPOSIT DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Amount Input
                        val minVal = if (selectedCurrency == "USDT") USDT_DEPOSIT_MIN else PHP_DEPOSIT_MIN
                        val maxVal = if (selectedCurrency == "USDT") USDT_DEPOSIT_MAX else PHP_DEPOSIT_MAX
                        val minFormatted = if (selectedCurrency == "USDT") "50 USDT" else "₱3,000"
                        val maxFormatted = if (selectedCurrency == "USDT") "5,000 USDT" else "₱100,000"

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = {
                                amountInput = it
                                localValidationError = null
                            },
                            label = { Text("Deposit Amount ($selectedCurrency)", fontSize = 12.sp) },
                            placeholder = { Text("Enter amount between $minFormatted and $maxFormatted", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B1322),
                                unfocusedContainerColor = Color(0xFF0B1322),
                                focusedBorderColor = CpiGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = CpiGold,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_input_amount")
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Limits: Minimum $minFormatted • Maximum $maxFormatted",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reference Number / Transaction Hash Input
                        OutlinedTextField(
                            value = referenceInput,
                            onValueChange = {
                                referenceInput = it
                                localValidationError = null
                            },
                            label = { Text(if (selectedCurrency == "PHP") "Payment Reference / Trace No." else "TxHash / Transaction Hash", fontSize = 12.sp) },
                            placeholder = { Text(if (selectedCurrency == "PHP") "e.g. GCash Ref # 100293847" else "e.g. 0x8a92... or TRC20 Hash", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B1322),
                                unfocusedContainerColor = Color(0xFF0B1322),
                                focusedBorderColor = CpiGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = CpiGold,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_input_reference")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Notes / Remark (Optional)
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Sender Note / Bank Name (Optional)", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Sent via Juan Dela Cruz BDO", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0B1322),
                                unfocusedContainerColor = Color(0xFF0B1322),
                                focusedBorderColor = CpiGold,
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = CpiGold,
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("deposit_input_notes")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compliance & Manual Approval Notice
                        Surface(
                            color = Color(0xFF1E293B).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CpiGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All deposits undergo strict manual compliance verification before being credited to your portfolio. Status will remain PENDING REVIEW until approved.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Error Message Display
                    if (localValidationError != null || errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = localValidationError ?: errorMessage ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFCA5A5),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (successTx == null) {
                Button(
                    onClick = {
                        val parsedAmount = amountInput.toDoubleOrNull()
                        if (parsedAmount == null) {
                            localValidationError = "Please enter a valid numeric deposit amount."
                            return@Button
                        }

                        val minVal = if (selectedCurrency == "USDT") USDT_DEPOSIT_MIN else PHP_DEPOSIT_MIN
                        val maxVal = if (selectedCurrency == "USDT") USDT_DEPOSIT_MAX else PHP_DEPOSIT_MAX

                        if (parsedAmount < minVal || parsedAmount > maxVal) {
                            localValidationError = if (selectedCurrency == "USDT") {
                                "Deposit amount must be between 50 USDT and 5,000 USDT."
                            } else {
                                "Deposit amount must be between ₱3,000 and ₱100,000."
                            }
                            return@Button
                        }

                        if (referenceInput.trim().isEmpty()) {
                            localValidationError = "Please provide the official payment reference number or transaction hash."
                            return@Button
                        }

                        if (selectedAccount == null || !isAccountValid) {
                            localValidationError = "These payment details have expired. Please request new payment details."
                            return@Button
                        }

                        localValidationError = null
                        dashboardViewModel.submitDeposit(
                            userId = user.id,
                            paymentAccountId = selectedAccount.id,
                            amount = parsedAmount,
                            currency = selectedCurrency,
                            paymentMethod = selectedAccount.paymentMethod,
                            referenceNo = referenceInput,
                            notes = notesInput
                        )
                    },
                    enabled = isAccountValid && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CpiGold,
                        contentColor = CpiNavy,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("submit_deposit_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = CpiNavy,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submitting...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit for Verification", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            if (successTx == null) {
                OutlinedButton(
                    onClick = {
                        dashboardViewModel.clearDepositStatus()
                        onDismiss()
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun PaymentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun CopyablePaymentRow(
    label: String,
    value: String,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isCopied) "Copied!" else "Copy",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCopied) Color(0xFF34D399) else CpiGold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = if (isCopied) Color(0xFF34D399) else CpiGold,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = CpiGold,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun DepositSuccessReceipt(
    tx: TransactionEntity,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFF10B981), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF34D399),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Deposit Submitted Successfully",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Status: PENDING REVIEW",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFBBF24)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                PaymentDetailRow("Tracking ID:", tx.id)
                Spacer(modifier = Modifier.height(6.dp))
                PaymentDetailRow("Amount:", "${tx.amount} ${tx.currency}")
                Spacer(modifier = Modifier.height(6.dp))
                PaymentDetailRow("Reference / Hash:", tx.reference)
                Spacer(modifier = Modifier.height(6.dp))
                PaymentDetailRow("Payment Channel:", tx.paymentMethod)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Your submission is now recorded in the platform ledger. CPI administrative officers will audit the transaction and credit your balance upon approval.",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done", fontWeight = FontWeight.Bold)
        }
    }
}
