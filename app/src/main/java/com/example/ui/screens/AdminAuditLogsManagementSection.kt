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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.local.AuditLogEntity
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy

@Composable
fun AdminAuditLogsManagementSection(
    auditLogs: List<AuditLogEntity>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var targetTypeFilter by remember { mutableStateOf("ALL") }
    var dateRangeFilter by remember { mutableStateOf(DateRangeOption.ALL) }
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 12

    val targetTypes = listOf("ALL", "USER", "TRANSACTION", "WITHDRAWAL", "INVESTMENT", "PAYMENT_ACCOUNT", "SECURITY")

    val filteredLogs = auditLogs.filter { log ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            val q = searchQuery.trim().lowercase()
            log.id.lowercase().contains(q) ||
            log.adminEmail.lowercase().contains(q) ||
            log.adminId.lowercase().contains(q) ||
            log.action.lowercase().contains(q) ||
            log.targetId.lowercase().contains(q) ||
            log.reason.lowercase().contains(q) ||
            log.valueChange.lowercase().contains(q)
        }

        val matchesType = if (targetTypeFilter == "ALL") true else {
            log.targetType.equals(targetTypeFilter, ignoreCase = true)
        }

        val matchesDate = isTimestampWithinRange(log.timestamp, dateRangeFilter)

        matchesSearch && matchesType && matchesDate
    }.sortedByDescending { it.timestamp }

    val totalPages = maxOf(1, (filteredLogs.size + pageSize - 1) / pageSize)
    val pagedLogs = filteredLogs.drop(currentPage * pageSize).take(pageSize)

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CpiGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Immutable Audit Trail & Security Logs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Total Audited Events: ${auditLogs.size} • Cryptographically Append-Only",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                currentPage = 0
            },
            placeholder = { Text("Search by Admin Email, Log ID, Target ID, Action, Reason...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                .testTag("admin_audit_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Target Type Filter Row
        val filterScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(filterScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            Text("Resource:", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

            targetTypes.forEach { type ->
                val isSelected = targetTypeFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        targetTypeFilter = type
                        currentPage = 0
                    },
                    label = { Text(type, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CpiGold,
                        selectedLabelColor = CpiNavy,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_audit_type_${type.lowercase()}")
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
            totalItems = filteredLogs.size,
            pageSize = pageSize,
            onPageChange = { currentPage = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Logs List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No audit log records match the current filter criteria.",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pagedLogs, key = { it.id }) { log ->
                    AuditLogItemCard(log = log)
                }
            }
        }
    }
}

@Composable
fun AuditLogItemCard(log: AuditLogEntity) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF131D2E),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_${log.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CpiGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.targetType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CpiGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.action,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = formatTimestamp(log.timestamp),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Admin: ${log.adminEmail} (UID: ${log.adminId})",
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )

            if (log.targetId.isNotBlank()) {
                Text(
                    text = "Target ID: ${log.targetId}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            if (log.valueChange.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = log.valueChange,
                        fontSize = 11.sp,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (log.reason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${log.reason}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Log ID: ${log.id}",
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
