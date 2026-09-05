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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.local.InvestmentEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

@Composable
fun AdminInvestmentsManagementSection(
    adminUser: UserEntity,
    investments: List<InvestmentEntity>,
    onUpdateInvestment: (String, String, Double, Double, Double, String) -> Unit,
    onViewUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var currencyFilter by remember { mutableStateOf("ALL") }
    var dateRangeFilter by remember { mutableStateOf(DateRangeOption.ALL) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 10

    var selectedInvForEdit by remember { mutableStateOf<InvestmentEntity?>(null) }
    var editStatus by remember { mutableStateOf("ACTIVE") }
    var editCurrentVal by remember { mutableStateOf("") }
    var editRealizedReturn by remember { mutableStateOf("") }
    var editPerfPct by remember { mutableStateOf("") }
    var editReason by remember { mutableStateOf("") }

    val filteredInvestments = investments.filter { inv ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            inv.id.lowercase().contains(q) ||
            inv.userId.lowercase().contains(q) ||
            inv.currency.lowercase().contains(q) ||
            inv.network.lowercase().contains(q)
        }

        val matchesStatus = if (statusFilter == "ALL") true else {
            inv.status.equals(statusFilter, ignoreCase = true)
        }

        val matchesCurrency = if (currencyFilter == "ALL") true else {
            inv.currency.equals(currencyFilter, ignoreCase = true)
        }

        val matchesDate = isTimestampWithinRange(inv.createdAt, dateRangeFilter)

        matchesSearch && matchesStatus && matchesCurrency && matchesDate
    }.sortedByDescending { it.createdAt }

    val totalPages = maxOf(1, (filteredInvestments.size + pageSize - 1) / pageSize)
    val pagedInvestments = filteredInvestments.drop(currentPage * pageSize).take(pageSize)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Column {
            Text(
                text = "Active Portfolios & Performance Oversight",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Portfolios: ${investments.size} • Active: ${investments.count { it.status == "ACTIVE" }}",
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
            placeholder = { Text("Search by Investment ID, User ID, Plan Name...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                .testTag("admin_investments_search_input")
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

            listOf("ALL", "ACTIVE", "COMPLETED", "CANCELLED", "PENDING").forEach { st ->
                val isSelected = statusFilter == st
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        statusFilter = st
                        currentPage = 0
                    },
                    label = { Text(st, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_inv_status_${st.lowercase()}")
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
                    modifier = Modifier.testTag("filter_inv_curr_${curr.lowercase()}")
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
            totalItems = filteredInvestments.size,
            pageSize = pageSize,
            onPageChange = { currentPage = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Investment Cards List
        if (filteredInvestments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No investments found matching the specified filters.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedInvestments, key = { it.id }) { inv ->
                    InvestmentManagementCard(
                        investment = inv,
                        onViewUser = { onViewUser(inv.userId) },
                        onEdit = {
                            selectedInvForEdit = inv
                            editStatus = inv.status
                            editCurrentVal = inv.currentValue.toString()
                            editRealizedReturn = inv.realizedReturn.toString()
                            editPerfPct = inv.performancePercentage.toString()
                            editReason = "Regular yield settlement / market update"
                        }
                    )
                }
            }
        }
    }

    // Edit Investment Performance Dialog
    if (selectedInvForEdit != null) {
        val inv = selectedInvForEdit!!

        AlertDialog(
            onDismissRequest = { selectedInvForEdit = null },
            title = {
                Text(
                    text = "Update Investment Valuation & Status",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Position #${inv.id.takeLast(8)} (${inv.currency} • ${inv.durationHours}h)",
                        color = CpiGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Initial Principal: ${inv.currency} ${"%,.2f".format(inv.amount)}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = editStatus,
                        onValueChange = { editStatus = it.uppercase() },
                        label = { Text("Status (ACTIVE, COMPLETED, CANCELLED)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_inv_status_input")
                    )

                    OutlinedTextField(
                        value = editCurrentVal,
                        onValueChange = { editCurrentVal = it },
                        label = { Text("Current Value (${inv.currency})", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_inv_current_val_input")
                    )

                    OutlinedTextField(
                        value = editPerfPct,
                        onValueChange = { editPerfPct = it },
                        label = { Text("Performance Percentage (%)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_inv_perf_input")
                    )

                    OutlinedTextField(
                        value = editReason,
                        onValueChange = { editReason = it },
                        label = { Text("Audit Reason", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth().testTag("edit_inv_reason_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cVal = editCurrentVal.toDoubleOrNull() ?: inv.currentValue
                        val rReturn = editRealizedReturn.toDoubleOrNull() ?: inv.realizedReturn
                        val perf = editPerfPct.toDoubleOrNull() ?: inv.performancePercentage
                        onUpdateInvestment(inv.id, editStatus, cVal, rReturn, perf, editReason.ifBlank { "Performance settlement" })
                        selectedInvForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                    modifier = Modifier.testTag("admin_confirm_inv_update_btn")
                ) {
                    Text("Save & Audit", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvForEdit = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun InvestmentManagementCard(
    investment: InvestmentEntity,
    onViewUser: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inv_row_${investment.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${investment.currency} Portfolio (${investment.durationHours}h)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                AdminStatusBadge(status = investment.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Principal: ${investment.currency} ${"%,.2f".format(investment.amount)}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                Text("Current: ${investment.currency} ${"%,.2f".format(investment.currentValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CpiGold)
                Text("Perf: +${investment.performancePercentage}%", fontSize = 12.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("User ID: ${investment.userId}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(formatTimestamp(investment.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
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
                    modifier = Modifier.testTag("view_user_from_inv_${investment.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect User", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }

                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = CpiGold, contentColor = CpiNavy),
                    modifier = Modifier.testTag("edit_inv_btn_${investment.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust & Settle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
