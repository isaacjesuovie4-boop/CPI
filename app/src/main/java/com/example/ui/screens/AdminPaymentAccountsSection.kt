package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PaymentAccountEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy
import kotlinx.coroutines.delay

@Composable
fun AdminPaymentAccountsSection(
    adminUser: UserEntity,
    accounts: List<PaymentAccountEntity>,
    onAddAccount: (String, String, String?, String?, String?, String?, String, Boolean) -> Unit,
    onUpdateAccount: (String, String, String?, String?, String?, String?, String) -> Unit,
    onPublishAccount: (String, Int) -> Unit,
    onUnpublishAccount: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var currencyFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedAccountForEdit by remember { mutableStateOf<PaymentAccountEntity?>(null) }
    var selectedAccountForPublish by remember { mutableStateOf<PaymentAccountEntity?>(null) }
    var publishDurationInput by remember { mutableStateOf("30") }

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Add form states
    var addCurrency by remember { mutableStateOf("USDT") }
    var addMethod by remember { mutableStateOf("USDT TRC20") }
    var addNetwork by remember { mutableStateOf("TRC20") }
    var addAccountName by remember { mutableStateOf("") }
    var addAccountNumber by remember { mutableStateOf("") }
    var addWalletAddress by remember { mutableStateOf("") }
    var addInstructions by remember { mutableStateOf("") }

    val filteredAccounts = accounts.filter { acc ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            (acc.accountName != null && acc.accountName.lowercase().contains(q)) ||
            (acc.accountNumber != null && acc.accountNumber.lowercase().contains(q)) ||
            (acc.walletAddress != null && acc.walletAddress.lowercase().contains(q)) ||
            (acc.network != null && acc.network.lowercase().contains(q)) ||
            acc.paymentMethod.lowercase().contains(q)
        }

        val matchesCurrency = if (currencyFilter == "ALL") true else {
            acc.currency.equals(currencyFilter, ignoreCase = true)
        }

        matchesSearch && matchesCurrency
    }.sortedByDescending { it.createdAt }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Payment Gateways & Receiving Channels",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Total Channels: ${accounts.size} • Published: ${accounts.count { it.isCurrentlyValid(currentTimeMs) }} active",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("admin_add_payment_account_btn")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Channel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search payment accounts, addresses, banks...", fontSize = 13.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedBorderColor = CpiGold,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_payment_accounts_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Row
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            Text("Currency:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            listOf("ALL", "USDT", "PHP").forEach { curr ->
                val isSelected = currencyFilter == curr
                FilterChip(
                    selected = isSelected,
                    onClick = { currencyFilter = curr },
                    label = { Text(curr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_pay_curr_${curr.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Accounts List
        if (filteredAccounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No payment accounts configured.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAccounts, key = { it.id }) { acc ->
                    PaymentAccountManagementCard(
                        account = acc,
                        currentTimeMs = currentTimeMs,
                        onEdit = { selectedAccountForEdit = acc },
                        onPublish = {
                            selectedAccountForPublish = acc
                            publishDurationInput = "30"
                        },
                        onUnpublish = { onUnpublishAccount(acc.id) },
                        onToggleActive = { onToggleActive(acc.id, it) }
                    )
                }
            }
        }
    }

    // Edit Channel Dialog
    if (selectedAccountForEdit != null) {
        EditPaymentAccountDialog(
            account = selectedAccountForEdit!!,
            onDismiss = { selectedAccountForEdit = null },
            onConfirmSave = { accId, method, network, accName, accNum, walletAddr, instructions ->
                onUpdateAccount(
                    accId,
                    method,
                    network,
                    accName,
                    accNum,
                    walletAddr,
                    instructions
                )
                selectedAccountForEdit = null
            }
        )
    }

    // Add Channel Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Configure New Payment Account", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = addCurrency == "USDT",
                            onClick = {
                                addCurrency = "USDT"
                                addMethod = "USDT TRC20"
                                addNetwork = "TRC20"
                            },
                            label = { Text("USDT (Crypto)") }
                        )
                        FilterChip(
                            selected = addCurrency == "PHP",
                            onClick = {
                                addCurrency = "PHP"
                                addMethod = "GCash"
                                addNetwork = ""
                            },
                            label = { Text("PHP (Fiat)") }
                        )
                    }

                    OutlinedTextField(
                        value = addMethod,
                        onValueChange = { addMethod = it },
                        label = { Text("Payment Method (e.g. GCash, Maya, USDT TRC20)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (addCurrency == "USDT") {
                        OutlinedTextField(
                            value = addNetwork,
                            onValueChange = { addNetwork = it },
                            label = { Text("Network (e.g. TRC20, BEP20)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = addWalletAddress,
                            onValueChange = { addWalletAddress = it },
                            label = { Text("Receiving Wallet Address (for USDT)", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = addAccountName,
                            onValueChange = { addAccountName = it },
                            label = { Text("Beneficiary / Account Name", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = addAccountNumber,
                            onValueChange = { addAccountNumber = it },
                            label = { Text("Account Number / Mobile Number", fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = addInstructions,
                        onValueChange = { addInstructions = it },
                        label = { Text("Deposit Instructions", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addAccountName.isNotBlank() || addAccountNumber.isNotBlank() || addWalletAddress.isNotBlank()) {
                            onAddAccount(
                                addCurrency,
                                addMethod,
                                addNetwork.ifBlank { null },
                                addAccountName.ifBlank { null },
                                addAccountNumber.ifBlank { null },
                                addWalletAddress.ifBlank { null },
                                addInstructions,
                                true
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy)
                ) {
                    Text("Create Channel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Publish Account Dialog
    if (selectedAccountForPublish != null) {
        val acc = selectedAccountForPublish!!
        AlertDialog(
            onDismissRequest = { selectedAccountForPublish = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Payment Details to Investors", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Account: ${acc.accountName ?: acc.walletAddress ?: acc.id} (${acc.currency} • ${acc.paymentMethod})",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Publication Validity Window (Minutes):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = publishDurationInput,
                        onValueChange = { publishDurationInput = it },
                        label = { Text("Minutes (Default: 30)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Published accounts will automatically expire and be hidden from investors after the validity window expires.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dur = publishDurationInput.toIntOrNull() ?: 30
                        onPublishAccount(acc.id, dur)
                        selectedAccountForPublish = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                ) {
                    Text("Publish Details", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedAccountForPublish = null }) { Text("Cancel", color = Color(0xFF94A3B8)) }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun PaymentAccountManagementCard(
    account: PaymentAccountEntity,
    currentTimeMs: Long,
    onEdit: () -> Unit,
    onPublish: () -> Unit,
    onUnpublish: () -> Unit,
    onToggleActive: (Boolean) -> Unit
) {
    val isValid = account.isCurrentlyValid(currentTimeMs)
    val isExpired = account.isExpired(currentTimeMs)
    val remainingSec = account.getRemainingSeconds(currentTimeMs)
    val remainingFormatted = remember(remainingSec) {
        val m = remainingSec / 60
        val s = remainingSec % 60
        "%02d:%02d".format(m, s)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier.fillMaxWidth().testTag("payment_account_row_${account.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = account.accountName ?: account.paymentMethod,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (account.currency == "USDT") Color(0xFF0284C7) else Color(0xFFD97706),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = account.currency,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (account.isActive) "Active" else "Inactive", fontSize = 11.sp, color = if (account.isActive) Color(0xFF34D399) else Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = account.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF10B981),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Method: ${account.paymentMethod}${if (!account.network.isNullOrBlank()) " • Network: ${account.network}" else ""}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
            if (!account.accountNumber.isNullOrBlank()) {
                Text("Account No / ID: ${account.accountNumber}", fontSize = 12.sp, color = CpiGold, fontWeight = FontWeight.SemiBold)
            }

            if (!account.walletAddress.isNullOrBlank()) {
                Text("Wallet Address: ${account.walletAddress}", fontSize = 11.sp, color = Color(0xFF94A3B8))
            }

            if (account.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Instructions: ${account.instructions}", fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 2)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                if (account.isPublished) {
                    if (isValid) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PUBLISHED • Valid $remainingFormatted",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                    } else if (isExpired) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PUBLICATION EXPIRED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFF64748B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "INACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "UNPUBLISHED (HIDDEN)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Action Buttons: Edit + Publish/Unpublish
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B), contentColor = CpiGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("edit_payment_account_${account.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (account.isPublished && isValid) {
                        OutlinedButton(
                            onClick = onUnpublish,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Unpublish", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = onPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("publish_payment_account_${account.id}")
                        ) {
                            Text("Publish (30m)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPaymentAccountDialog(
    account: PaymentAccountEntity,
    onDismiss: () -> Unit,
    onConfirmSave: (
        id: String,
        paymentMethod: String,
        network: String?,
        accountName: String?,
        accountNumber: String?,
        walletAddress: String?,
        instructions: String
    ) -> Unit
) {
    var editPaymentMethod by remember(account) { mutableStateOf(account.paymentMethod) }
    var editNetwork by remember(account) { mutableStateOf(account.network ?: "TRC20") }
    var editAccountName by remember(account) { mutableStateOf(account.accountName ?: "") }
    var editAccountNumber by remember(account) { mutableStateOf(account.accountNumber ?: "") }
    var editWalletAddress by remember(account) { mutableStateOf(account.walletAddress ?: "") }
    var editInstructions by remember(account) { mutableStateOf(account.instructions) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isUsdt = account.currency.equals("USDT", ignoreCase = true)

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CpiGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirm Payment Details Update",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "You are about to update payment configuration for:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Account ID: ${account.id}",
                                fontSize = 11.sp,
                                color = CpiGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Currency: ${account.currency}",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isUsdt) {
                                Text(
                                    text = "Network: $editNetwork",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "Wallet Address: ${editWalletAddress.trim()}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            } else {
                                Text(
                                    text = "Payment Method: ${editPaymentMethod.trim()}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "Account Name: ${editAccountName.trim()}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "Account Number: ${editAccountNumber.trim()}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            if (editInstructions.isNotBlank()) {
                                Text(
                                    text = "Instructions: ${editInstructions.trim()}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ Notice: Any active deposit sessions will immediately reflect these new payment details.",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val method = if (isUsdt) "USDT ${editNetwork.trim()}" else editPaymentMethod.trim()
                        val net = if (isUsdt) editNetwork.trim() else null
                        val name = if (!isUsdt) editAccountName.trim().ifBlank { null } else null
                        val num = if (!isUsdt) editAccountNumber.trim().ifBlank { null } else null
                        val wallet = if (isUsdt) editWalletAddress.trim().ifBlank { null } else null

                        onConfirmSave(
                            account.id,
                            method,
                            net,
                            name,
                            num,
                            wallet,
                            editInstructions.trim()
                        )
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("confirm_save_payment_account_btn")
                ) {
                    Text("Yes, Save & Update", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Back to Editing", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = CpiGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit ${account.currency} Payment Gateway",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Document ID: ${account.id} • ${account.currency}",
                    fontSize = 11.sp,
                    color = CpiGold,
                    fontWeight = FontWeight.SemiBold
                )

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFF87171),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (!isUsdt) {
                    // PHP Fiat Fields
                    OutlinedTextField(
                        value = editPaymentMethod,
                        onValueChange = {
                            editPaymentMethod = it
                            errorMessage = null
                        },
                        label = { Text("Payment Method / Provider", fontSize = 11.sp) },
                        placeholder = { Text("e.g. GCash, Maya, BDO Bank Transfer", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_payment_method_input")
                    )

                    OutlinedTextField(
                        value = editAccountName,
                        onValueChange = {
                            editAccountName = it
                            errorMessage = null
                        },
                        label = { Text("Account Name / Beneficiary", fontSize = 11.sp) },
                        placeholder = { Text("e.g. CPI Global Operations Inc.", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_account_name_input")
                    )

                    OutlinedTextField(
                        value = editAccountNumber,
                        onValueChange = {
                            editAccountNumber = it
                            errorMessage = null
                        },
                        label = { Text("Account Number / Mobile Number", fontSize = 11.sp) },
                        placeholder = { Text("e.g. 0917-123-4567 or 001234567890", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_account_number_input")
                    )
                } else {
                    // USDT Crypto Fields
                    Text(
                        text = "Network Selection:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TRC20", "BEP20").forEach { net ->
                            FilterChip(
                                selected = editNetwork.equals(net, ignoreCase = true),
                                onClick = {
                                    editNetwork = net
                                    editPaymentMethod = "USDT $net"
                                    errorMessage = null
                                },
                                label = { Text(net, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CpiGold,
                                    selectedLabelColor = CpiNavy,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = null,
                                modifier = Modifier.testTag("edit_network_chip_$net")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editWalletAddress,
                        onValueChange = {
                            editWalletAddress = it
                            errorMessage = null
                        },
                        label = { Text("Receiving Wallet Address ($editNetwork)", fontSize = 11.sp) },
                        placeholder = {
                            Text(
                                text = if (editNetwork == "TRC20") "Enter TRC20 address (starts with T...)" else "Enter BEP20 address (starts with 0x...)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_wallet_address_input")
                    )
                }

                OutlinedTextField(
                    value = editInstructions,
                    onValueChange = { editInstructions = it },
                    label = { Text("Payment Instructions for Investors", fontSize = 11.sp) },
                    placeholder = {
                        Text(
                            text = "e.g. Send exact amount and keep transaction reference number.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CpiGold,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_instructions_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation
                    if (isUsdt) {
                        if (editWalletAddress.trim().isBlank()) {
                            errorMessage = "Please enter a valid receiving wallet address."
                            return@Button
                        }
                    } else {
                        if (editAccountName.trim().isBlank() && editAccountNumber.trim().isBlank()) {
                            errorMessage = "Please provide an account name or account number."
                            return@Button
                        }
                    }
                    errorMessage = null
                    showConfirmDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("save_payment_account_btn")
            ) {
                Text("Review & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
