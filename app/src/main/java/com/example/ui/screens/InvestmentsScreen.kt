package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvestmentEntity
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.components.ContactSupportSection
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekCardBorder
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

@Composable
fun InvestmentsScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val investments by dashboardViewModel.investments.collectAsState()
    val overview by dashboardViewModel.overview.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(currentUser) {
        dashboardViewModel.setUserId(currentUser?.id)
    }

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
                        contentDescription = "Login Required",
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
                        text = "Please log in to view your investment portfolio history.",
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

    val filteredInvestments = remember(investments, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "ACTIVE" -> investments.filter { it.status == "ACTIVE" }
            "COMPLETED" -> investments.filter { it.status == "COMPLETED" }
            "PENDING" -> investments.filter { it.status == "PENDING" }
            "CANCELLED" -> investments.filter { it.status == "CANCELLED" }
            else -> investments
        }
    }

    val preferredCurrency = currentUser?.selectedCurrency?.ifBlank { "USDT" } ?: "USDT"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onNavigate("dashboard") },
                    modifier = Modifier.testTag("investments_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SleekTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Investment Portfolios",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Verified positions & active yield tracking",
                        fontSize = 12.sp,
                        color = SleekTextSecondary
                    )
                }
            }

            Button(
                onClick = { onNavigate("invest") },
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("investments_new_allocation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Invest Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Metric Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PortfolioMetricCard(
                title = "Total Invested",
                value = formatCurrencyValue(overview.totalInvested, preferredCurrency),
                sub = "${investments.size} Total Positions",
                color = SleekPrimary,
                modifier = Modifier.weight(1f)
            )
            PortfolioMetricCard(
                title = "Current Value",
                value = formatCurrencyValue(overview.currentInvestmentValue, preferredCurrency),
                sub = "${overview.activeInvestmentsCount} Active Portfolios",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL" to "All (${investments.size})",
                "ACTIVE" to "Active (${investments.count { it.status == "ACTIVE" }})",
                "COMPLETED" to "Completed (${investments.count { it.status == "COMPLETED" }})",
                "PENDING" to "Pending (${investments.count { it.status == "PENDING" }})",
                "CANCELLED" to "Cancelled (${investments.count { it.status == "CANCELLED" }})"
            ).forEach { (statusKey, label) ->
                val isSelected = selectedStatusFilter == statusKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStatusFilter = statusKey },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SleekPrimaryContainer,
                        selectedLabelColor = SleekPrimary,
                        containerColor = SleekSurfaceVariant,
                        labelColor = SleekTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = SleekCardBorder,
                        selectedBorderColor = SleekPrimaryBorder
                    ),
                    modifier = Modifier.testTag("filter_investments_${statusKey.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Investment List or Zero State
        if (filteredInvestments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SleekSurface),
                    border = BorderStroke(1.dp, SleekCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = SleekSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (investments.isEmpty()) "No investments yet." else "No ${selectedStatusFilter.lowercase()} investments found.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (investments.isEmpty()) {
                                "Start your institutional portfolio allocation with USDT (50–5,000) or PHP (₱3,000–₱100,000)."
                            } else {
                                "Try selecting a different status filter above to inspect other portfolio positions."
                            },
                            fontSize = 12.sp,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        if (investments.isEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { onNavigate("invest") },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("empty_state_invest_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Invest Now",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredInvestments, key = { it.id }) { investment ->
                    InvestmentCard(investment = investment)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ContactSupportSection()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun InvestmentCard(investment: InvestmentEntity) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val startDateStr = remember(investment.startAt) { dateFormat.format(Date(investment.startAt)) }
    val endDateStr = remember(investment.endAt) { dateFormat.format(Date(investment.endAt)) }

    val statusColor = when (investment.status.uppercase()) {
        "ACTIVE" -> Color(0xFF10B981)
        "COMPLETED" -> Color(0xFF3B82F6)
        "PENDING" -> Color(0xFFF59E0B)
        "CANCELLED" -> Color(0xFFEF4444)
        else -> SleekPrimary
    }

    val formattedAmount = remember(investment.amount, investment.currency) {
        if (investment.currency == "PHP") {
            "₱${NumberFormat.getNumberInstance(Locale.US).format(investment.amount)}"
        } else {
            "${NumberFormat.getNumberInstance(Locale.US).format(investment.amount)} USDT"
        }
    }

    val formattedCurrentVal = remember(investment.currentValue, investment.currency) {
        if (investment.currency == "PHP") {
            "₱${NumberFormat.getNumberInstance(Locale.US).format(investment.currentValue)}"
        } else {
            "${NumberFormat.getNumberInstance(Locale.US).format(investment.currentValue)} USDT"
        }
    }

    val formattedRealizedReturn = remember(investment.realizedReturn, investment.currency) {
        if (investment.currency == "PHP") {
            "₱${NumberFormat.getNumberInstance(Locale.US).format(investment.realizedReturn)}"
        } else {
            "${NumberFormat.getNumberInstance(Locale.US).format(investment.realizedReturn)} USDT"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_card_${investment.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (investment.status == "ACTIVE") Icons.Default.TrendingUp else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = investment.id,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "${investment.durationHours} Hours Cycle • ${if (investment.network.isNotBlank()) investment.network else investment.currency}",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = investment.status.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = SleekCardBorder
            )

            // Performance & Financial Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Invested Principal", fontSize = 11.sp, color = SleekTextMuted)
                    Text(
                        text = formattedAmount,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Current Value", fontSize = 11.sp, color = SleekTextMuted)
                    Text(
                        text = formattedCurrentVal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (investment.currentValue >= investment.amount) Color(0xFF10B981) else SleekPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(text = "Performance", fontSize = 11.sp, color = SleekTextMuted)
                    val perfText = if (investment.performancePercentage >= 0) {
                        "+${String.format(Locale.US, "%.2f", investment.performancePercentage)}%"
                    } else {
                        "${String.format(Locale.US, "%.2f", investment.performancePercentage)}%"
                    }
                    Text(
                        text = perfText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (investment.performancePercentage > 0) Color(0xFF10B981) else if (investment.performancePercentage < 0) Color(0xFFEF4444) else SleekPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dates Row
            Surface(
                color = SleekSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SleekTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start: $startDateStr",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }

                    Text(
                        text = "Maturity: $endDateStr",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                }
            }

            if (investment.realizedReturn > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Realized Yield Credited:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "+$formattedRealizedReturn",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioMetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SleekTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = sub,
                fontSize = 10.sp,
                color = SleekTextMuted
            )
        }
    }
}

private fun formatCurrencyValue(amount: Double, currency: String): String {
    return if (currency == "PHP") {
        "₱${NumberFormat.getNumberInstance(Locale.US).format(amount)}"
    } else {
        "${NumberFormat.getNumberInstance(Locale.US).format(amount)} USDT"
    }
}
