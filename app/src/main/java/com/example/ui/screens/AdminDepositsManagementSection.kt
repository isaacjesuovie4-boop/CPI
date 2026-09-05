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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

@Composable
fun AdminDepositsManagementSection(
    adminUser: UserEntity,
    deposits: List<TransactionEntity>,
    onUpdateStatus: (String, String, String) -> Unit,
    onViewUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var currencyFilter by remember { mutableStateOf("ALL") }
    var dateRangeFilter by remember { mutableStateOf(DateRangeOption.ALL) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 10

    var selectedDepositForAction by remember { mutableStateOf<TransactionEntity?>(null) }
    var actionTargetStatus by remember { mutableStateOf("") }
    var actionReason by remember { mutableStateOf("") }

    val filteredDeposits = deposits.filter { dep ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            dep.id.lowercase().contains(q) ||
            dep.userId.lowercase().contains(q) ||
            (dep.reference != null && dep.reference.lowercase().contains(q)) ||
            (dep.paymentMethod != null && dep.paymentMethod.lowercase().contains(q)) ||
            (dep.notes != null && dep.notes.lowercase().contains(q))
        }

        val matchesStatus = if (statusFilter == "ALL") true else {
            dep.status.equals(statusFilter, ignoreCase = true)
        }

        val matchesCurrency = if (currencyFilter == "ALL") true else {
            dep.currency.equals(currencyFilter, ignoreCase = true)
        }

        val matchesDate = isTimestampWithinRange(dep.createdAt, dateRangeFilter)

        matchesSearch && matchesStatus && matchesCurrency && matchesDate
    }.sortedByDescending { it.createdAt }

    val totalPages = maxOf(1, (filteredDeposits.size + pageSize - 1) / pageSize)
    val pagedDeposits = filteredDeposits.drop(currentPage * pageSize).take(pageSize)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Column {
            Text(
                text = "Deposit Verification & Reconciliation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Deposits: ${deposits.size} • Pending: ${deposits.count { it.status == "PENDING" }}",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                currentPage = 0
            },
            placeholder = { Text("Search by Tx ID, User ID, Reference Number, Payment Method...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                .testTag("admin_deposits_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Row
        val filterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            Text("Status:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            listOf("ALL", "PENDING", "COMPLETED", "REJECTED", "EXPIRED").forEach { st ->
                val isSelected = statusFilter == st
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        statusFilter = st
                        currentPage = 0
                    },
                    label = { Text(if (st == "COMPLETED") "APPROVED" else st, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_deposit_status_${st.lowercase()}")
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text("Currency:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            listOf("ALL", "USDT", "PHP").forEach { curr ->
                val isSelected = currencyFilter == curr
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        currencyFilter = curr
                        currentPage = 0
                    },
                    label = { Text(curr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_deposit_curr_${curr.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Date Range Filter
        DateRangeFilterRow(
            selectedOption = dateRangeFilter,
            onOptionSelected = {
                dateRangeFilter = it
                currentPage = 0
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pagination Bar
        PaginationControlsBar(
            currentPage = currentPage,
            totalPages = totalPages,
            totalItems = filteredDeposits.size,
            pageSize = pageSize,
            onPageChange = { currentPage = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Deposit Cards List
        if (filteredDeposits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No deposits found matching the specified filters.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedDeposits, key = { it.id }) { dep ->
                    DepositManagementCard(
                        deposit = dep,
                        onViewUser = { onViewUser(dep.userId) },
                        onApprove = {
                            selectedDepositForAction = dep
                            actionTargetStatus = "COMPLETED"
                            actionReason = "Proof of payment verified against merchant ledger"
                        },
                        onReject = {
                            selectedDepositForAction = dep
                            actionTargetStatus = "REJECTED"
                            actionReason = "Reference not found or mismatch on bank statement"
                        }
                    )
                }
            }
        }
    }

    // Action Confirmation Dialog
    if (selectedDepositForAction != null) {
        val dep = selectedDepositForAction!!
        val isApprove = actionTargetStatus == "COMPLETED"

        AlertDialog(
            onDismissRequest = { selectedDepositForAction = null },
            title = {
                Text(
                    text = if (isApprove) "Approve & Credit Deposit" else "Reject Deposit Request",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Deposit ${dep.currency} ${"%,.2f".format(dep.amount)} from User ${dep.userId}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tx ID: ${dep.id} • Ref: ${dep.reference ?: "N/A"}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        label = { Text("Audit / Decision Reason", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = if (isApprove) Color(0xFF10B981) else Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_deposit_action_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(dep.id, actionTargetStatus, actionReason.ifBlank { "Administrative decision" })
                        selectedDepositForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) Color(0xFF059669) else Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_confirm_deposit_action_btn")
                ) {
                    Text(if (isApprove) "Confirm Approval" else "Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDepositForAction = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun DepositManagementCard(
    deposit: TransactionEntity,
    onViewUser: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deposit_row_${deposit.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${deposit.currency} ${"%,.2f".format(deposit.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                AdminStatusBadge(status = deposit.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tx ID: ${deposit.id}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text("Method: ${deposit.paymentMethod ?: "Direct"}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            }

            if (!deposit.reference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("Reference: ${deposit.reference}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CpiGold)
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("User ID: ${deposit.userId}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(formatTimestamp(deposit.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewUser,
                    modifier = Modifier.testTag("view_user_from_dep_${deposit.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect User", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }

                if (deposit.status == "PENDING") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                            modifier = Modifier.testTag("reject_dep_btn_${deposit.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), contentColor = Color.White),
                            modifier = Modifier.testTag("approve_dep_btn_${deposit.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
