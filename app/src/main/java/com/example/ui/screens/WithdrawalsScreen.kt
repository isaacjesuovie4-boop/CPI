package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.WithdrawalEntity
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WithdrawalsScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val withdrawals by dashboardViewModel.withdrawals.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }

    val user = currentUser
    if (user == null) {
        LaunchedEffect(Unit) {
            onNavigate("login")
        }
        return
    }

    val filteredList = remember(withdrawals, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> withdrawals.filter { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" || it.status == "PENDING" }
            "COMPLETED" -> withdrawals.filter { it.status == "COMPLETED" || it.status == "APPROVED" }
            "REJECTED" -> withdrawals.filter { it.status == "REJECTED" || it.status == "CANCELLED" }
            else -> withdrawals
        }
    }

    val totalWithdrawn = remember(withdrawals) {
        withdrawals.filter { it.status == "COMPLETED" || it.status == "APPROVED" }.sumOf { it.amount }
    }

    val pendingCount = remember(withdrawals) {
        withdrawals.count { it.status == "PENDING_REVIEW" || it.status == "PROCESSING" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
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
                        modifier = Modifier.testTag("withdrawals_back_button")
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
                            text = "Withdrawal Records",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Settlement & Payout Audit Trail",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Button(
                    onClick = { onNavigate("withdraw") },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("new_withdrawal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TOTAL COMPLETED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "$%,.2f", totalWithdrawn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SleekSuccess
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("IN REVIEW / QUEUED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekTextMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$pendingCount Requests",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (pendingCount > 0) SleekWarning else SleekTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                "ALL" to "All Requests",
                "PENDING" to "In Review",
                "COMPLETED" to "Completed",
                "REJECTED" to "Rejected"
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPrimary,
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = SleekSurface,
                        labelColor = SleekTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedFilter == key) SleekPrimary else SleekCardBorder,
                        enabled = true,
                        selected = selectedFilter == key
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Withdrawals List
        if (filteredList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(SleekSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = SleekTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No withdrawal requests yet.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Request a capital withdrawal from your available balance anytime.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { onNavigate("withdraw") },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Initiate First Withdrawal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.withdrawalId }) { withdrawal ->
                    WithdrawalItemCard(withdrawal = withdrawal)
                }
            }
        }
    }
}

@Composable
fun WithdrawalItemCard(withdrawal: WithdrawalEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(withdrawal.createdAt))

    val statusColor = when (withdrawal.status) {
        "COMPLETED", "APPROVED" -> SleekSuccess
        "PENDING_REVIEW", "PENDING" -> SleekWarning
        "PROCESSING" -> Color(0xFF38BDF8) // Bright Sky Blue
        "REJECTED", "CANCELLED" -> SleekError
        else -> SleekTextSecondary
    }

    val statusIcon = when (withdrawal.status) {
        "COMPLETED", "APPROVED" -> Icons.Default.CheckCircle
        "PENDING_REVIEW", "PENDING" -> Icons.Default.HourglassEmpty
        "PROCESSING" -> Icons.Default.Sync
        "REJECTED", "CANCELLED" -> Icons.Default.Close
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("withdrawal_item_${withdrawal.withdrawalId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = withdrawal.withdrawalId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SleekPrimary
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = withdrawal.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amount & Destination
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Requested Amount", fontSize = 10.sp, color = SleekTextMuted)
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", withdrawal.amount)} ${withdrawal.currency}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SleekTextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Network / Method", fontSize = 10.sp, color = SleekTextMuted)
                    Text(
                        text = withdrawal.network,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = SleekCardBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Destination Account / Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Destination Details:", fontSize = 11.sp, color = SleekTextMuted)
                Text(
                    text = withdrawal.destination,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextPrimary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Submission Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Submitted:", fontSize = 11.sp, color = SleekTextMuted)
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = SleekTextSecondary
                )
            }

            // Rejection reason if applicable
            if (!withdrawal.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SleekErrorContainer,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, SleekErrorBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Rejection Reason:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekError
                        )
                        Text(
                            text = withdrawal.rejectionReason,
                            fontSize = 11.sp,
                            color = SleekOnErrorContainer,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Processed timestamp if completed
            if (withdrawal.processedAt != null && (withdrawal.status == "COMPLETED" || withdrawal.status == "APPROVED")) {
                Spacer(modifier = Modifier.height(6.dp))
                val processedFormatted = dateFormat.format(Date(withdrawal.processedAt))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Processed & Dispatched:", fontSize = 10.sp, color = SleekSuccess)
                    Text(
                        text = processedFormatted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekSuccess
                    )
                }
            }
        }
    }
}
