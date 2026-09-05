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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
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
import com.example.data.local.TransactionEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

@Composable
fun AdminTransactionsManagementSection(
    transactions: List<TransactionEntity>,
    onViewUser: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("ALL") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var currencyFilter by remember { mutableStateOf("ALL") }
    var dateRangeFilter by remember { mutableStateOf(DateRangeOption.ALL) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 12

    val filteredTransactions = transactions.filter { tx ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            tx.id.lowercase().contains(q) ||
            tx.userId.lowercase().contains(q) ||
            (tx.reference != null && tx.reference.lowercase().contains(q)) ||
            (tx.notes != null && tx.notes.lowercase().contains(q)) ||
            (tx.paymentMethod != null && tx.paymentMethod.lowercase().contains(q))
        }

        val matchesType = if (typeFilter == "ALL") true else {
            tx.type.equals(typeFilter, ignoreCase = true)
        }

        val matchesStatus = if (statusFilter == "ALL") true else {
            tx.status.equals(statusFilter, ignoreCase = true)
        }

        val matchesCurrency = if (currencyFilter == "ALL") true else {
            tx.currency.equals(currencyFilter, ignoreCase = true)
        }

        val matchesDate = isTimestampWithinRange(tx.createdAt, dateRangeFilter)

        matchesSearch && matchesType && matchesStatus && matchesCurrency && matchesDate
    }.sortedByDescending { it.createdAt }

    val totalPages = maxOf(1, (filteredTransactions.size + pageSize - 1) / pageSize)
    val pagedTransactions = filteredTransactions.drop(currentPage * pageSize).take(pageSize)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Section Header
        Column {
            Text(
                text = "Master Financial Ledger & Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Total Records: ${transactions.size} • Immutable Transaction Trail",
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
            placeholder = { Text("Search by Tx ID, User ID, Reference, Notes...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                .testTag("admin_transactions_search_input")
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
            Text("Type:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            listOf("ALL", "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "RETURN").forEach { tp ->
                val isSelected = typeFilter == tp
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        typeFilter = tp
                        currentPage = 0
                    },
                    label = { Text(tp, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_tx_type_${tp.lowercase()}")
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text("Status:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            listOf("ALL", "COMPLETED", "PENDING", "REJECTED").forEach { st ->
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
                    modifier = Modifier.testTag("filter_tx_status_${st.lowercase()}")
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
            totalItems = filteredTransactions.size,
            pageSize = pageSize,
            onPageChange = { currentPage = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found matching the filter criteria.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedTransactions, key = { it.id }) { tx ->
                    TransactionManagementCard(
                        transaction = tx,
                        onViewUser = { onViewUser(tx.userId) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionManagementCard(
    transaction: TransactionEntity,
    onViewUser: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_row_${transaction.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = CpiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${transaction.type} • ${transaction.currency} ${"%,.2f".format(transaction.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                AdminStatusBadge(status = transaction.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tx ID: ${transaction.id}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                if (!transaction.reference.isNullOrBlank()) {
                    Text("Ref: ${transaction.reference}", fontSize = 11.sp, color = CpiGold)
                }
            }

            if (!transaction.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text("Notes: ${transaction.notes}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("User ID: ${transaction.userId}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Text(formatTimestamp(transaction.createdAt), fontSize = 10.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewUser,
                    modifier = Modifier.testTag("view_user_from_tx_${transaction.id}")
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspect User Profile", fontSize = 11.sp, color = Color(0xFF38BDF8))
                }
            }
        }
    }
}
