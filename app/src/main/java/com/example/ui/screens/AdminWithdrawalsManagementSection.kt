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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
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
import com.example.data.local.UserEntity
import com.example.data.local.WithdrawalEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

@Composable
fun AdminWithdrawalsManagementSection(
    adminUser: UserEntity,
    withdrawals: List<WithdrawalEntity>,
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

    var selectedWthForAction by remember { mutableStateOf<WithdrawalEntity?>(null) }
    var targetStatus by remember { mutableStateOf("") }
    var actionReason by remember { mutableStateOf("") }

    val filteredWithdrawals = withdrawals.filter { wth ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            wth.withdrawalId.lowercase().contains(q) ||
            wth.userId.lowercase().contains(q) ||
            wth.destination.lowercase().contains(q) ||
            wth.network.lowercase().contains(q) ||
            (wth.transactionId != null && wth.transactionId.lowercase().contains(q)) ||
            (wth.rejectionReason != null && wth.rejectionReason.lowercase().contains(q))
        }

        val matchesStatus = if (statusFilter == "ALL") true else {
            wth.status.equals(statusFilter, ignoreCase = true)
        }

        val matchesCurrency = if (currencyFilter == "ALL") true else {
            wth.currency.equals(currencyFilter, ignoreCase = true)
        }

        val matchesDate = isTimestampWithinRange(wth.createdAt, dateRangeFilter)

        matchesSearch && matchesStatus && matchesCurrency && matchesDate
    }.sortedByDescending { it.createdAt }

    val totalPages = maxOf(1, (filteredWithdrawals.size + pageSize - 1) / pageSize)
    val pagedWithdrawals = filteredWithdrawals.drop(currentPage * pageSize).take(pageSize)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Column {
            Text(
                text = "Withdrawal Requests & Payout Queue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Requests: ${withdrawals.size} • Pending/Processing: ${withdrawals.count { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" }}",
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
            placeholder = { Text("Search by Withdrawal ID, User ID, Destination, Network...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                .testTag("admin_withdrawals_search_input")
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

            listOf("ALL", "PENDING_REVIEW", "PROCESSING", "COMPLETED", "REJECTED").forEach { st ->
                val isSelected = statusFilter == st
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        statusFilter = st
                        currentPage = 0
                    },
                    label = { Text(st.replace("_", " "), fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_wth_status_${st.lowercase()}")
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
                    modifier = Modifier.testTag("filter_wth_curr_${curr.lowercase()}")
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
            totalItems = filteredWithdrawals.size,
            pageSize = pageSize,
            onPageChange = { currentPage = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Withdrawal Cards List
        if (filteredWithdrawals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No withdrawal requests match current filters.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedWithdrawals, key = { it.withdrawalId }) { wth ->
                    WithdrawalManagementCard(
                        withdrawal = wth,
                        onViewUser = { onViewUser(wth.userId) },
                        onProcess = {
                            selectedWthForAction = wth
                            targetStatus = "PROCESSING"
                            actionReason = "Dispatched to liquidity provider / payout processor"
                        },
                        onComplete = {
                            selectedWthForAction = wth
                            targetStatus = "COMPLETED"
                            actionReason = "Blockchain transaction confirmed / bank wire executed"
                        },
                        onReject = {
                            selectedWthForAction = wth
                            targetStatus = "REJECTED"
                            actionReason = "Invalid payout address or compliance verification failure"
                        }
                    )
                }
            }
        }
    }

    // Action Confirmation Dialog
    if (selectedWthForAction != null) {
        val wth = selectedWthForAction!!

        AlertDialog(
            onDismissRequest = { selectedWthForAction = null },
            title = {
                Text(
                    text = "Update Withdrawal to $targetStatus",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Withdrawal: ${wth.currency} ${"%,.2f".format(wth.amount)}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Destination: ${wth.destination} (${wth.network})",
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
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_wth_action_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(wth.withdrawalId, targetStatus, actionReason.ifBlank { "Administrative transition" })
                        selectedWthForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                    modifier = Modifier.testTag("admin_confirm_wth_action_btn")
                ) {
                    Text("Confirm Transition", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWthForAction = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun WithdrawalManagementCard(
    withdrawal: WithdrawalEntity,
    onViewUser: () -> Unit,
    onProcess: () -> Unit,
    onComplete: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_row_${withdrawal.withdrawalId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${withdrawal.currency} ${"%,.2f".format(withdrawal.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                AdminStatusBadge(status = withdrawal.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text("Network: ${withdrawal.network}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            Text("Destination: ${withdrawal.destination}", fontSize = 11.sp, color = Color(0xFF38BDF8))

            if (!withdrawal.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("Rejection Note: ${withdrawal.rejectionReason}", fontSize = 11.sp, color = Color(0xFFF87171))
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("User ID: ${withdrawal.userId}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(formatTimestamp(withdrawal.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
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
                    modifier = Modifier.testTag("view_user_from_wth_${withdrawal.withdrawalId}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect User", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (withdrawal.status == "PENDING_REVIEW") {
                        Button(
                            onClick = onProcess,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6), contentColor = Color.White),
                            modifier = Modifier.testTag("process_wth_btn_${withdrawal.withdrawalId}")
                        ) {
                            Text("Process", fontSize = 11.sp)
                        }
                    }

                    if (withdrawal.status == "PENDING_REVIEW" || withdrawal.status == "PROCESSING") {
                        Button(
                            onClick = onReject,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626), contentColor = Color.White),
                            modifier = Modifier.testTag("reject_wth_btn_${withdrawal.withdrawalId}")
                        ) {
                            Text("Reject", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669), contentColor = Color.White),
                            modifier = Modifier.testTag("complete_wth_btn_${withdrawal.withdrawalId}")
                        ) {
                            Text("Complete", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
