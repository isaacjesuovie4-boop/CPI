package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvestmentEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

enum class UserDetailTab(val label: String) {
    OVERVIEW("Financial Summary"),
    INVESTMENTS("Investments"),
    DEPOSITS("Deposits"),
    WITHDRAWALS("Withdrawals"),
    TRANSACTIONS("Transactions"),
    NOTIFICATIONS("Notifications")
}

@Composable
fun AdminUserDetailsSection(
    adminUser: UserEntity,
    user: UserEntity?,
    investments: List<InvestmentEntity>,
    transactions: List<TransactionEntity>,
    withdrawals: List<WithdrawalEntity>,
    notifications: List<NotificationEntity>,
    onSuspendUser: (String, String) -> Unit,
    onReactivateUser: (String, String) -> Unit,
    onAddAdminNote: (String, String) -> Unit,
    onBack: () -> Unit
) {
    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("User not found or no user selected.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy)
                ) {
                    Text("Return to Users List")
                }
            }
        }
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(UserDetailTab.OVERVIEW) }
    var showSuspendDialog by remember { mutableStateOf(false) }
    var showReactivateDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var suspendReason by remember { mutableStateOf("") }
    var reactivateReason by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val userDeposits = transactions.filter { it.type.equals("DEPOSIT", ignoreCase = true) }
    val totalDeposited = userDeposits.filter { it.status == "COMPLETED" || it.status == "APPROVED" }.sumOf { it.amount }
    val pendingDepositsSum = userDeposits.filter { it.status == "PENDING" }.sumOf { it.amount }
    val pendingDepositsCount = userDeposits.count { it.status == "PENDING" }

    val totalInvested = investments.sumOf { it.amount }
    val activeInvested = investments.filter { it.status == "ACTIVE" }.sumOf { it.amount }

    val totalWithdrawn = withdrawals.filter { it.status == "COMPLETED" }.sumOf { it.amount }
    val pendingWithdrawalsSum = withdrawals.filter { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" }.sumOf { it.amount }
    val pendingWithdrawalsCount = withdrawals.count { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" }

    // Net estimated balance: approved deposits + returns - invested - withdrawn
    val returnsSum = transactions.filter { it.type.equals("RETURN", ignoreCase = true) && it.status == "COMPLETED" }.sumOf { it.amount }
    val availableBalance = maxOf(0.0, totalDeposited + returnsSum - totalInvested - totalWithdrawn)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("user_details_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Users",
                        tint = CpiGold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "User Profile & Audit View",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "User ID: ${user.id}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick Status Pill
            AdminStatusBadge(status = user.accountStatus)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Details Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CpiGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CpiGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = user.email,
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Copy ID button
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(user.id)) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))

                // Profile Fields Grid
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PHONE NUMBER", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(user.phoneNumber.ifBlank { "N/A" }, fontSize = 13.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COUNTRY", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(user.country.ifBlank { "N/A" }, fontSize = 13.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OCCUPATION", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(user.occupation.ifBlank { "N/A" }, fontSize = 13.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BASE CURRENCY", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(user.selectedCurrency, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CpiGold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("REGISTRATION DATE", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(formatTimestamp(user.createdAt), fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ACCOUNT ROLE", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        Text(user.role, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Administrative Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (user.accountStatus.equals("SUSPENDED", ignoreCase = true)) {
                        Button(
                            onClick = { showReactivateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_reactivate_user_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reactivate Account", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { showSuspendDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_suspend_user_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Suspend Account", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showAddNoteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CpiGold),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_add_note_btn")
                    ) {
                        Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Admin Note", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Tabs
        val tabScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(tabScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserDetailTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(tab.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("user_tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Tab Content
        when (selectedTab) {
            UserDetailTab.OVERVIEW -> {
                // Financial Summary Cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialMetricCard(
                            title = "AVAILABLE BALANCE",
                            value = "${user.selectedCurrency} ${"%,.2f".format(availableBalance)}",
                            subtext = "Live computed balance",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            title = "TOTAL DEPOSITED",
                            value = "${user.selectedCurrency} ${"%,.2f".format(totalDeposited)}",
                            subtext = "${userDeposits.count { it.status == "COMPLETED" || it.status == "APPROVED" }} approved deposits",
                            icon = Icons.Default.AccountBalance,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialMetricCard(
                            title = "TOTAL INVESTED",
                            value = "${user.selectedCurrency} ${"%,.2f".format(totalInvested)}",
                            subtext = "${investments.size} portfolios (${investments.count { it.status == "ACTIVE" }} active)",
                            icon = Icons.Default.TrendingUp,
                            color = CpiGold,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            title = "TOTAL WITHDRAWN",
                            value = "${user.selectedCurrency} ${"%,.2f".format(totalWithdrawn)}",
                            subtext = "${withdrawals.count { it.status == "COMPLETED" }} completed withdrawals",
                            icon = Icons.Default.MonetizationOn,
                            color = Color(0xFFEC4899),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FinancialMetricCard(
                            title = "PENDING DEPOSITS",
                            value = "${user.selectedCurrency} ${"%,.2f".format(pendingDepositsSum)}",
                            subtext = "$pendingDepositsCount awaiting review",
                            icon = Icons.Default.AccountBalanceWallet,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricCard(
                            title = "PENDING WITHDRAWALS",
                            value = "${user.selectedCurrency} ${"%,.2f".format(pendingWithdrawalsSum)}",
                            subtext = "$pendingWithdrawalsCount in queue / processing",
                            icon = Icons.Default.MonetizationOn,
                            color = Color(0xFFF97316),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            UserDetailTab.INVESTMENTS -> {
                if (investments.isEmpty()) {
                    EmptySectionPlaceholder("No investment portfolios found for this user.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        investments.forEach { inv ->
                            Surface(
                                color = Color(0xFF131D2E),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${inv.currency} Position (${inv.durationHours}h)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        AdminStatusBadge(status = inv.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Investment ID: ${inv.id}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Initial: ${inv.currency} ${"%,.2f".format(inv.amount)}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                        Text("Current: ${inv.currency} ${"%,.2f".format(inv.currentValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CpiGold)
                                        Text("Return: +${inv.performancePercentage}%", fontSize = 12.sp, color = Color(0xFF34D399))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            UserDetailTab.DEPOSITS -> {
                if (userDeposits.isEmpty()) {
                    EmptySectionPlaceholder("No deposit records found for this user.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        userDeposits.forEach { dep ->
                            Surface(
                                color = Color(0xFF131D2E),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${dep.currency} ${"%,.2f".format(dep.amount)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        AdminStatusBadge(status = dep.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Tx ID: ${dep.id} • Method: ${dep.paymentMethod ?: "N/A"}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    if (!dep.reference.isNullOrBlank()) {
                                        Text("Ref: ${dep.reference}", fontSize = 11.sp, color = CpiGold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatTimestamp(dep.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
            UserDetailTab.WITHDRAWALS -> {
                if (withdrawals.isEmpty()) {
                    EmptySectionPlaceholder("No withdrawal records found for this user.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        withdrawals.forEach { wth ->
                            Surface(
                                color = Color(0xFF131D2E),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${wth.currency} ${"%,.2f".format(wth.amount)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        AdminStatusBadge(status = wth.status)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("ID: ${wth.withdrawalId} • Network: ${wth.network}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text("Destination: ${wth.destination}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                    if (!wth.rejectionReason.isNullOrBlank()) {
                                        Text("Rejection: ${wth.rejectionReason}", fontSize = 11.sp, color = Color(0xFFF87171))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatTimestamp(wth.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
            UserDetailTab.TRANSACTIONS -> {
                if (transactions.isEmpty()) {
                    EmptySectionPlaceholder("No transaction ledger records found for this user.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.forEach { tx ->
                            Surface(
                                color = Color(0xFF131D2E),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${tx.type} • ${tx.currency} ${"%,.2f".format(tx.amount)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Tx ID: ${tx.id} ${if (!tx.reference.isNullOrBlank()) "• Ref: ${tx.reference}" else ""}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                        Text(formatTimestamp(tx.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    AdminStatusBadge(status = tx.status)
                                }
                            }
                        }
                    }
                }
            }
            UserDetailTab.NOTIFICATIONS -> {
                if (notifications.isEmpty()) {
                    EmptySectionPlaceholder("No notification history found for this user.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notifications.forEach { notif ->
                            Surface(
                                color = Color(0xFF131D2E),
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(notif.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text(notif.type, fontSize = 10.sp, color = CpiGold, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(notif.message, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(formatTimestamp(notif.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Suspend Account Dialog
    if (showSuspendDialog) {
        AlertDialog(
            onDismissRequest = { showSuspendDialog = false },
            title = {
                Text("Confirm Account Suspension", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to suspend account for ${user.fullName} (${user.email})? The user will be barred from trading, depositing, and withdrawing.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = suspendReason,
                        onValueChange = { suspendReason = it },
                        placeholder = { Text("Mandatory reason for suspension...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_suspend_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSuspendUser(user.id, suspendReason.ifBlank { "Administrative policy enforcement" })
                        showSuspendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                    modifier = Modifier.testTag("admin_confirm_suspend_btn")
                ) {
                    Text("Suspend Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSuspendDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Reactivate Account Dialog
    if (showReactivateDialog) {
        AlertDialog(
            onDismissRequest = { showReactivateDialog = false },
            title = {
                Text("Confirm Account Reactivation", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Reactivate account for ${user.fullName} (${user.email})? Full platform access will be restored.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reactivateReason,
                        onValueChange = { reactivateReason = it },
                        placeholder = { Text("Reason for reactivation...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_reactivate_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReactivateUser(user.id, reactivateReason.ifBlank { "Account verified and cleared" })
                        showReactivateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), contentColor = Color.White),
                    modifier = Modifier.testTag("admin_confirm_reactivate_btn")
                ) {
                    Text("Reactivate Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReactivateDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = {
                Text("Add Internal Administrative Note", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "This internal note will be permanently logged in the audit trail for user ${user.fullName}.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Enter internal notes / compliance observations...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_internal_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            onAddAdminNote(user.id, noteText.trim())
                            showAddNoteDialog = false
                            noteText = ""
                        }
                    },
                    enabled = noteText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                    modifier = Modifier.testTag("admin_save_note_btn")
                ) {
                    Text("Save to Audit Log", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun FinancialMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun EmptySectionPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color(0xFF94A3B8), fontSize = 13.sp)
    }
}
