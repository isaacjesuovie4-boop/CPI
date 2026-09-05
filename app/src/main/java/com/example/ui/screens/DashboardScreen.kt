package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvestmentEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.repository.DashboardOverview
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.QuickActionDialogType
import com.example.ui.components.ContactSupportSection
import com.example.ui.components.CpiLogo
import com.example.ui.components.DepositModal
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val overview by dashboardViewModel.overview.collectAsState()
    val investments by dashboardViewModel.investments.collectAsState()
    val transactions by dashboardViewModel.transactions.collectAsState()
    val dialogState by dashboardViewModel.dialogState.collectAsState()
    val selectedFilter by dashboardViewModel.selectedTransactionFilter.collectAsState()
    val unreadNotificationCount by dashboardViewModel.unreadNotificationCount.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Sync current user ID into DashboardViewModel
    LaunchedEffect(currentUser) {
        dashboardViewModel.setUserId(currentUser?.id)
    }

    if (currentUser == null) {
        // Protected Route fallback: redirect to login
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
                modifier = Modifier.fillMaxWidth(),
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
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Authentication Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SleekTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please log in to access your CPI Investor Dashboard.",
                        fontSize = 13.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { onNavigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Go to Login", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val user = currentUser!!
    val dateFormatted = remember(user.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(user.createdAt))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("dashboard_screen")
    ) {
        // 1. DASHBOARD HEADER / BAR
        DashboardHeader(
            user = user,
            unreadNotificationsCount = unreadNotificationCount,
            onNotificationClick = { onNavigate("notifications") },
            onProfileClick = { onNavigate("profile") },
            onLogoutClick = { showLogoutConfirmDialog = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. USER ACCOUNT SUMMARY CARD
        UserAccountSummaryCard(
            user = user,
            dateFormatted = dateFormatted,
            onEditProfile = { onNavigate("profile") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. BALANCE OVERVIEW SECTION (4 Cards: Total Invested, Current Value, Active, Completed)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PORTFOLIO OVERVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMuted,
                letterSpacing = 1.sp
            )

            Text(
                text = "View Portfolios →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekPrimary,
                modifier = Modifier
                    .clickable { onNavigate("investments") }
                    .testTag("dashboard_view_all_investments_top_link")
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        BalanceOverviewGrid(
            overview = overview,
            currency = user.selectedCurrency
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Prominent "Invest Now" Hero Action Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_invest_now_banner"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
            border = BorderStroke(1.dp, SleekPrimaryBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Grow Your Capital",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekOnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (user.selectedCurrency == "USDT") "Allocate 50 – 5,000 USDT in institutional yield cycles" else "Allocate ₱3,000 – ₱100,000 in secure portfolios",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }

                Button(
                    onClick = { onNavigate("invest") },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dashboard_invest_now_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Invest Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. QUICK ACTIONS
        Text(
            text = "QUICK ACTIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        QuickActionsSection(
            currency = user.selectedCurrency,
            onInvestClick = { onNavigate("invest") },
            onDepositClick = { dashboardViewModel.openDepositDialog() },
            onWithdrawClick = { onNavigate("withdraw") },
            onActionClick = { actionName, title, message ->
                dashboardViewModel.showActionNotice(actionName, title, message)
            },
            onTransactionsClick = {
                // Scroll down to transaction history
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. INVESTMENT PERFORMANCE SECTION
        InvestmentPerformanceSection(
            overview = overview,
            currency = user.selectedCurrency
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. CURRENCY RULES & LIMITS NOTICE
        CurrencyRulesBanner(currency = user.selectedCurrency)

        Spacer(modifier = Modifier.height(20.dp))

        // 7. ACTIVE INVESTMENTS SECTION
        ActiveInvestmentsSection(
            investments = investments,
            currency = user.selectedCurrency,
            onInvestNowClick = { onNavigate("invest") },
            onViewAllClick = { onNavigate("investments") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 8. TRANSACTION HISTORY SECTION
        TransactionHistorySection(
            transactions = transactions,
            selectedFilter = selectedFilter,
            currency = user.selectedCurrency,
            onFilterSelect = { dashboardViewModel.setTransactionFilter(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 9. STAGE 2 PROTOCOL NOTICE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CPI Development Stage 2: Investor Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your dashboard provides persistent portfolio and ledger oversight. Real deposit gateways, automated trade matching, and withdrawal processing will be integrated in subsequent milestones without fabricated data.",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 10. DIRECT SUPPORT CHANNELS
        ContactSupportSection()

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Quick Action Notice Dialog
    if (dialogState is QuickActionDialogType.ActionNotice) {
        val notice = dialogState as QuickActionDialogType.ActionNotice
        AlertDialog(
            onDismissRequest = { dashboardViewModel.dismissDialog() },
            containerColor = SleekSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = notice.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SleekTextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = notice.message,
                        fontSize = 13.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SleekSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Account: ${user.id} • Currency: ${user.selectedCurrency}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekPrimary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { dashboardViewModel.dismissDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Official Deposit Modal
    if (dialogState is QuickActionDialogType.Deposit) {
        DepositModal(
            user = user,
            dashboardViewModel = dashboardViewModel,
            onDismiss = { dashboardViewModel.dismissDialog() }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = SleekSurface,
            icon = {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = SleekError,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Sign Out of CPI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SleekTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to end your current session?",
                    fontSize = 13.sp,
                    color = SleekTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        authViewModel.logout()
                        onNavigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekError, contentColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = SleekTextMuted)
                }
            }
        )
    }
}

// ----------------------------------------------------
// 1. DASHBOARD HEADER COMPONENT
// ----------------------------------------------------
@Composable
private fun DashboardHeader(
    user: UserEntity,
    unreadNotificationsCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_header"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CpiLogo(iconSize = 28.dp, isDark = false)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Investor Dashboard",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Crest Point Investment",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notification Bell button
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("dashboard_notifications_button")
                ) {
                    if (unreadNotificationsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = SleekPrimary,
                                    contentColor = Color(0xFF0F172A)
                                ) {
                                    Text(
                                        text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications ($unreadNotificationsCount unread)",
                                tint = SleekPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = SleekTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Profile button
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.testTag("dashboard_profile_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Profile Settings",
                        tint = SleekPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Logout button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("dashboard_logout_action_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = SleekError,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. USER ACCOUNT SUMMARY CARD
// ----------------------------------------------------
@Composable
private fun UserAccountSummaryCard(
    user: UserEntity,
    dateFormatted: String,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_account_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SleekPrimaryContainer)
                            .border(1.dp, SleekPrimaryBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Account ID: ${user.id}",
                            fontSize = 12.sp,
                            color = SleekPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekPrimaryContainer)
                        .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SleekPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = user.accountStatus,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = SleekCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "BASE CURRENCY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted
                    )
                    Text(
                        text = user.selectedCurrency,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                }

                Column {
                    Text(
                        text = "ACCOUNT CREATED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted
                    )
                    Text(
                        text = dateFormatted,
                        fontSize = 12.sp,
                        color = SleekTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PROFILE SETTINGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted
                    )
                    Text(
                        text = "Edit Profile →",
                        fontSize = 12.sp,
                        color = SleekPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(onClick = onEditProfile)
                            .testTag("dashboard_edit_profile_link")
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// ----------------------------------------------------
// 3. BALANCE OVERVIEW GRID (4 CARDS: Available Balance, Total Invested, Total Withdrawn, Pending Withdrawals)
// ----------------------------------------------------
@Composable
private fun BalanceOverviewGrid(
    overview: DashboardOverview,
    currency: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BalanceCard(
                title = "Available Balance",
                amount = formatCurrencyAmount(overview.availableBalance, currency),
                icon = Icons.Default.AccountBalanceWallet,
                tag = "balance_card_available",
                modifier = Modifier.weight(1f),
                isPrimary = true
            )

            BalanceCard(
                title = "Total Invested",
                amount = formatCurrencyAmount(overview.totalInvested, currency),
                icon = Icons.Default.MonetizationOn,
                tag = "balance_card_invested",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BalanceCard(
                title = "Total Withdrawn",
                amount = formatCurrencyAmount(overview.totalWithdrawn, currency),
                icon = Icons.Default.CheckCircle,
                tag = "balance_card_withdrawn",
                modifier = Modifier.weight(1f)
            )

            BalanceCard(
                title = "Pending Withdrawals",
                amount = formatCurrencyAmount(overview.pendingWithdrawals, currency),
                icon = Icons.Default.History,
                tag = "balance_card_pending_withdrawals",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BalanceCard(
    title: String,
    amount: String,
    icon: ImageVector,
    tag: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier.testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) SleekPrimaryContainer else SleekSurface
        ),
        border = BorderStroke(1.dp, if (isPrimary) SleekPrimaryBorder else SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimary) SleekOnPrimaryContainer else SleekTextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = amount,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) SleekPrimary else SleekTextPrimary
            )
        }
    }
}

// ----------------------------------------------------
// 4. QUICK ACTIONS SECTION
// ----------------------------------------------------
@Composable
private fun QuickActionsSection(
    currency: String,
    onInvestClick: () -> Unit,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onActionClick: (actionName: String, title: String, message: String) -> Unit,
    onTransactionsClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                label = "INVEST",
                icon = Icons.Default.TrendingUp,
                tag = "button_quick_invest",
                isPrimary = true,
                modifier = Modifier.weight(1f),
                onClick = onInvestClick
            )

            QuickActionButton(
                label = "DEPOSIT",
                icon = Icons.Default.ArrowDownward,
                tag = "button_quick_deposit",
                modifier = Modifier.weight(1f),
                onClick = onDepositClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(
                label = "WITHDRAW",
                icon = Icons.Default.ArrowUpward,
                tag = "button_quick_withdraw",
                modifier = Modifier.weight(1f),
                onClick = onWithdrawClick
            )

            QuickActionButton(
                label = "TRANSACTIONS",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                tag = "button_quick_transactions",
                modifier = Modifier.weight(1f),
                onClick = onTransactionsClick
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    tag: String,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .height(48.dp)
                .testTag(tag)
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextPrimary),
            border = BorderStroke(1.dp, SleekCardBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .height(48.dp)
                .testTag(tag)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ----------------------------------------------------
// 5. INVESTMENT PERFORMANCE SECTION
// ----------------------------------------------------
@Composable
private fun InvestmentPerformanceSection(
    overview: DashboardOverview,
    currency: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("investment_performance_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Investment Performance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }

                Text(
                    text = "${overview.activeInvestmentsCount} Active",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PerformanceMetric(
                    label = "Total Invested",
                    value = formatCurrencyAmount(overview.totalInvested, currency)
                )

                PerformanceMetric(
                    label = "Current Value",
                    value = formatCurrencyAmount(overview.currentInvestmentValue, currency)
                )

                PerformanceMetric(
                    label = "Performance",
                    value = "${if (overview.overallPerformancePercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", overview.overallPerformancePercentage)}%",
                    isHighlight = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (overview.activeInvestmentsCount == 0 && overview.totalInvested == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SleekTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "No active investments",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No investment activity yet. Real investment performance metrics will appear here once an investment plan is activated.",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetric(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) SleekPrimary else SleekTextPrimary
        )
    }
}

// ----------------------------------------------------
// 6. CURRENCY RULES BANNER
// ----------------------------------------------------
@Composable
private fun CurrencyRulesBanner(currency: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("currency_rules_banner"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currency == "USDT") "USDT Account Parameters" else "PHP Account Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (currency == "USDT") {
                Text(
                    text = "• Minimum Investment: 50 USDT\n• Maximum Investment: 5,000 USDT\n• Supported Settlement Networks: TRC20, BEP20\n• Strict Currency Isolation: Never mixed with PHP",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 18.sp
                )
            } else {
                Text(
                    text = "• Minimum Investment: ₱3,000\n• Maximum Investment: ₱100,000\n• Supported Payment Methods: Direct Domestic Banking / Instant Gateway\n• Strict Currency Isolation: Never mixed with USDT",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// 7. ACTIVE INVESTMENTS SECTION
// ----------------------------------------------------
@Composable
private fun ActiveInvestmentsSection(
    investments: List<InvestmentEntity>,
    currency: String,
    onInvestNowClick: () -> Unit,
    onViewAllClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_investments_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recent Investments",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "${investments.size} active cycles recorded",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }

                if (investments.isNotEmpty()) {
                    Text(
                        text = "View All →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary,
                        modifier = Modifier
                            .clickable(onClick = onViewAllClick)
                            .testTag("dashboard_view_all_investments_section_link")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (investments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekSurfaceVariant)
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = SleekTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No active investments.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When an investment cycle is initiated, real-time tracking will be displayed here.",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onInvestNowClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("dashboard_empty_invest_now_button")
                        ) {
                            Text(
                                text = "Start Investment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            } else {
                investments.take(3).forEach { item ->
                    InvestmentItemRow(item = item, currency = currency)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (investments.size > 3) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onViewAllClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_see_more_investments_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SleekCardBorder)
                    ) {
                        Text(
                            text = "See All ${investments.size} Investments",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentItemRow(
    item: InvestmentEntity,
    currency: String
) {
    val dateStr = remember(item.createdAt) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(item.createdAt))
    }
    val statusBg = when (item.status.uppercase()) {
        "ACTIVE" -> SleekPrimaryContainer
        "COMPLETED" -> SleekSuccessContainer
        "CANCELLED" -> SleekErrorContainer
        else -> SleekSurface
    }
    val statusColor = when (item.status.uppercase()) {
        "ACTIVE" -> SleekPrimary
        "COMPLETED" -> SleekSuccess
        "CANCELLED" -> SleekError
        else -> SleekTextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("dashboard_investment_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ID: ${item.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    if (item.network != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SleekSurface)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.network,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Invested: ${formatCurrencyAmount(item.amount, item.currency)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Duration: ${item.durationHours} Hours • $dateStr",
                        fontSize = 10.sp,
                        color = SleekTextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Val: ${formatCurrencyAmount(item.currentValue, item.currency)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekPrimary
                    )
                    Text(
                        text = "${if (item.performancePercentage >= 0) "+" else ""}${String.format(Locale.US, "%.2f", item.performancePercentage)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.performancePercentage >= 0) SleekSuccess else SleekError
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 8. TRANSACTION HISTORY SECTION
// ----------------------------------------------------
@Composable
private fun TransactionHistorySection(
    transactions: List<TransactionEntity>,
    selectedFilter: String,
    currency: String,
    onFilterSelect: (String) -> Unit
) {
    var sortDescending by remember { mutableStateOf(true) }

    val filteredList = remember(transactions, selectedFilter, sortDescending) {
        val filtered = if (selectedFilter == "ALL") transactions
        else transactions.filter { it.type.equals(selectedFilter, ignoreCase = true) }

        if (sortDescending) {
            filtered.sortedByDescending { it.createdAt }
        } else {
            filtered.sortedBy { it.createdAt }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_history_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Transaction History",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "Auditable ledger records for deposits, withdrawals, and returns",
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }

                // Sort toggle (Newest / Oldest)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SleekSurfaceVariant)
                        .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
                        .clickable { sortDescending = !sortDescending }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("transaction_history_sort_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (sortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (sortDescending) "Newest" else "Oldest",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "DEPOSIT", "WITHDRAWAL", "INVESTMENT", "RETURN").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterSelect(filter) },
                        label = {
                            Text(
                                text = filter,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
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
                            selected = selectedFilter == filter,
                            borderColor = if (selectedFilter == filter) SleekPrimary else SleekCardBorder,
                            borderWidth = 1.dp
                        ),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                val emptyMsg = when (selectedFilter) {
                    "DEPOSIT" -> "No deposits have been recorded."
                    "WITHDRAWAL" -> "No withdrawals have been recorded."
                    "INVESTMENT" -> "No investment transactions recorded."
                    "RETURN" -> "No returns recorded."
                    else -> "No transactions yet."
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SleekSurfaceVariant)
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = SleekTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = emptyMsg,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Auditable records will be stored permanently upon confirmation.",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                filteredList.forEach { tx ->
                    TransactionItemRow(tx = tx)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun TransactionItemRow(tx: TransactionEntity) {
    val dateStr = remember(tx.createdAt) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SleekSurfaceVariant)
            .border(1.dp, SleekCardBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (tx.type) {
                    "DEPOSIT" -> Icons.Default.ArrowDownward
                    "WITHDRAWAL" -> Icons.Default.ArrowUpward
                    "INVESTMENT" -> Icons.Default.TrendingUp
                    else -> Icons.Default.AccountBalance
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tx.type,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    Text(
                        text = "${tx.id} • $dateStr",
                        fontSize = 10.sp,
                        color = SleekTextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrencyAmount(tx.amount, tx.currency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.type == "WITHDRAWAL") SleekError else SleekPrimary
                )
                Text(
                    text = tx.status,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekTextMuted
                )
            }
        }
    }
}

// ----------------------------------------------------
// UTILITY FUNCTIONS
// ----------------------------------------------------
private fun formatCurrencyAmount(amount: Double, currency: String): String {
    return if (currency.uppercase() == "PHP") {
        "₱" + NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }.format(amount)
    } else {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2
            maximumFractionDigits = 2
        }.format(amount) + " USDT"
    }
}
