package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditLogEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import com.example.data.repository.AdminOverviewStats
import com.example.ui.AdminSection
import com.example.ui.AdminViewModel
import com.example.ui.AuthViewModel
import com.example.ui.theme.CpiGold
import com.example.ui.theme.CpiNavy
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel,
    initialSection: AdminSection = AdminSection.DASHBOARD,
    onNavigate: (String) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentSection by adminViewModel.currentSection.collectAsState()
    val overviewStats by adminViewModel.overviewStats.collectAsState()
    val usersList by adminViewModel.usersList.collectAsState()
    val investmentsList by adminViewModel.investmentsList.collectAsState()
    val transactionsList by adminViewModel.transactionsList.collectAsState()
    val pendingDeposits by adminViewModel.pendingDepositsList.collectAsState()
    val pendingWithdrawals by adminViewModel.pendingWithdrawalsList.collectAsState()
    val allWithdrawals by adminViewModel.allWithdrawalsList.collectAsState()
    val auditLogs by adminViewModel.auditLogsList.collectAsState()
    val paymentAccounts by adminViewModel.paymentAccountsList.collectAsState()
    val adminNotifications by adminViewModel.adminNotificationsList.collectAsState()
    val adminUnreadCount by adminViewModel.adminUnreadCount.collectAsState()
    val actionMessage by adminViewModel.actionMessage.collectAsState()

    // Search and User selection states
    val globalSearchQuery by adminViewModel.globalSearchQuery.collectAsState()
    val globalSearchResults by adminViewModel.globalSearchResults.collectAsState()
    val selectedUserId by adminViewModel.selectedUserId.collectAsState()
    val selectedUser by adminViewModel.selectedUser.collectAsState()
    val selectedUserInvestments by adminViewModel.selectedUserInvestments.collectAsState()
    val selectedUserTransactions by adminViewModel.selectedUserTransactions.collectAsState()
    val selectedUserWithdrawals by adminViewModel.selectedUserWithdrawals.collectAsState()
    val selectedUserNotifications by adminViewModel.selectedUserNotifications.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialSection) {
        adminViewModel.setSection(initialSection)
        adminViewModel.refreshStats()
    }

    // ACCESS CONTROL VERIFICATION
    if (currentUser == null) {
        AccessDeniedView(
            title = "Authentication Required",
            message = "You must authenticate with valid administrator credentials to access the CPI Management Console.",
            buttonText = "Go to Admin Login",
            onAction = { onNavigate("admin/login") }
        )
        return
    }

    if (currentUser?.role != "ADMIN") {
        AccessDeniedView(
            title = "ACCESS DENIED — Administrator Privileges Required",
            message = "Your authenticated account (${currentUser?.email}) is registered as an Investor. Access to the CPI Platform Administration Console is restricted to authorized operations personnel.",
            buttonText = "Return to Investor Dashboard",
            onAction = { onNavigate("dashboard") },
            showLogout = true,
            onLogout = {
                authViewModel.logout()
                onNavigate("login")
            }
        )
        return
    }

    val adminUser = currentUser!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin Top Bar
            AdminHeaderBar(
                adminUser = adminUser,
                onLogoutClick = { showLogoutDialog = true },
                onNavigateToInvestorDashboard = { onNavigate("dashboard") }
            )

            // Global Search Bar Component
            AdminGlobalSearchBar(
                query = globalSearchQuery,
                onQueryChange = { adminViewModel.setGlobalSearchQuery(it) },
                onClear = { adminViewModel.clearGlobalSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            AnimatedVisibility(visible = globalSearchQuery.isNotBlank()) {
                AdminGlobalSearchResultsList(
                    results = globalSearchResults,
                    onSelectResult = { res ->
                        adminViewModel.clearGlobalSearch()
                        when (res.targetSection) {
                            AdminSection.USERS -> {
                                adminViewModel.selectUser(res.targetId)
                                adminViewModel.setSection(AdminSection.USERS)
                            }
                            else -> adminViewModel.setSection(res.targetSection)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Section Navigation Bar / Tabs
            AdminNavigationTabs(
                selectedSection = currentSection,
                onSectionSelected = {
                    adminViewModel.setSection(it)
                    if (it != AdminSection.USERS) {
                        adminViewModel.selectUser(null)
                    }
                },
                pendingDepositsCount = overviewStats.pendingDepositsCount,
                pendingWithdrawalsCount = overviewStats.pendingWithdrawalsCount,
                unreadNotificationsCount = adminUnreadCount
            )

            // Action Notification Banner
            AnimatedVisibility(visible = actionMessage != null) {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = actionMessage ?: "",
                                color = Color(0xFFD1FAE5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(
                            onClick = { adminViewModel.clearActionMessage() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Main Content Area based on selected section
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (currentSection) {
                    AdminSection.DASHBOARD -> AdminDashboardOverviewSection(
                        stats = overviewStats,
                        pendingDeposits = pendingDeposits,
                        pendingWithdrawals = pendingWithdrawals,
                        recentAuditLogs = auditLogs.take(5),
                        onNavigateSection = { adminViewModel.setSection(it) },
                        onRefresh = { adminViewModel.refreshStats() }
                    )
                    AdminSection.NOTIFICATIONS -> AdminNotificationsSection(
                        adminUser = adminUser,
                        notifications = adminNotifications,
                        unreadCount = adminUnreadCount,
                        onMarkAsRead = { adminViewModel.markAdminNotificationAsRead(it) },
                        onMarkAllAsRead = { adminViewModel.markAllAdminNotificationsAsRead() },
                        onNavigateToSection = { adminViewModel.setSection(it) }
                    )
                    AdminSection.USERS -> {
                        if (selectedUserId != null) {
                            AdminUserDetailsSection(
                                adminUser = adminUser,
                                user = selectedUser,
                                investments = selectedUserInvestments,
                                transactions = selectedUserTransactions,
                                withdrawals = selectedUserWithdrawals,
                                notifications = selectedUserNotifications,
                                onSuspendUser = { uid, reason ->
                                    adminViewModel.suspendUser(adminUser, uid, reason)
                                },
                                onReactivateUser = { uid, reason ->
                                    adminViewModel.reactivateUser(adminUser, uid, reason)
                                },
                                onAddAdminNote = { uid, note ->
                                    adminViewModel.addUserAdminNote(adminUser, uid, note)
                                },
                                onBack = { adminViewModel.selectUser(null) }
                            )
                        } else {
                            AdminUsersManagementSection(
                                users = usersList,
                                onSelectUser = { uid -> adminViewModel.selectUser(uid) }
                            )
                        }
                    }
                    AdminSection.INVESTMENTS -> AdminInvestmentsManagementSection(
                        adminUser = adminUser,
                        investments = investmentsList,
                        onUpdateInvestment = { invId, newStatus, newCurrentVal, newReturn, newPerf, reason ->
                            adminViewModel.updateInvestment(
                                adminUser = adminUser,
                                investmentId = invId,
                                newStatus = newStatus,
                                newCurrentValue = newCurrentVal,
                                newRealizedReturn = newReturn,
                                newPerformancePercentage = newPerf,
                                reason = reason
                            )
                        },
                        onViewUser = { uid ->
                            adminViewModel.selectUser(uid)
                            adminViewModel.setSection(AdminSection.USERS)
                        }
                    )
                    AdminSection.DEPOSITS -> AdminDepositsManagementSection(
                        adminUser = adminUser,
                        deposits = transactionsList.filter { it.type == "DEPOSIT" },
                        onUpdateStatus = { txId, status, reason ->
                            adminViewModel.updateTransactionStatus(adminUser, txId, status, reason)
                        },
                        onViewUser = { uid ->
                            adminViewModel.selectUser(uid)
                            adminViewModel.setSection(AdminSection.USERS)
                        }
                    )
                    AdminSection.PAYMENT_ACCOUNTS -> AdminPaymentAccountsSection(
                        adminUser = adminUser,
                        accounts = paymentAccounts,
                        onAddAccount = { curr, method, net, name, num, addr, inst, autoPub ->
                            adminViewModel.addPaymentAccount(adminUser, curr, method, net, name, num, addr, inst, autoPub)
                        },
                        onUpdateAccount = { id, method, net, name, num, addr, inst ->
                            adminViewModel.updatePaymentAccount(adminUser, id, method, net, name, num, addr, inst)
                        },
                        onPublishAccount = { id, dur ->
                            adminViewModel.publishPaymentAccount(adminUser, id, dur)
                        },
                        onUnpublishAccount = { id ->
                            adminViewModel.unpublishPaymentAccount(adminUser, id)
                        },
                        onToggleActive = { id, active ->
                            adminViewModel.togglePaymentAccountActive(adminUser, id, active)
                        }
                    )
                    AdminSection.WITHDRAWALS -> AdminWithdrawalsManagementSection(
                        adminUser = adminUser,
                        withdrawals = allWithdrawals,
                        onUpdateStatus = { withdrawalId, status, reason ->
                            adminViewModel.updateWithdrawalStatus(adminUser, withdrawalId, status, reason)
                        },
                        onViewUser = { uid ->
                            adminViewModel.selectUser(uid)
                            adminViewModel.setSection(AdminSection.USERS)
                        }
                    )
                    AdminSection.TRANSACTIONS -> AdminTransactionsManagementSection(
                        transactions = transactionsList,
                        onViewUser = { uid ->
                            adminViewModel.selectUser(uid)
                            adminViewModel.setSection(AdminSection.USERS)
                        }
                    )
                    AdminSection.AUDIT_LOGS -> AdminAuditLogsManagementSection(
                        auditLogs = auditLogs
                    )
                    AdminSection.SETTINGS -> AdminAuditLogsManagementSection(
                        auditLogs = auditLogs
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = CpiGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terminate Admin Session?", color = Color.White)
                }
            },
            text = {
                Text(
                    "You are about to sign out of the CPI Administrative Console. You will need to re-authenticate with admin credentials to access management features.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onNavigate("admin/login")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_confirm_logout_button")
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

// Top Bar for Admin
@Composable
private fun AdminHeaderBar(
    adminUser: UserEntity,
    onLogoutClick: () -> Unit,
    onNavigateToInvestorDashboard: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF1E293B), Color(0xFF334155))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(CpiGold.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, CpiGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Admin Shield",
                        tint = CpiGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "CREST POINT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CpiGold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = adminUser.email,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onNavigateToInvestorDashboard,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("admin_switch_to_investor_view")
                ) {
                    Text("Investor View", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("admin_topbar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color(0xFFF87171)
                    )
                }
            }
        }
    }
}

// Navigation Tabs for Admin
@Composable
private fun AdminNavigationTabs(
    selectedSection: AdminSection,
    onSectionSelected: (AdminSection) -> Unit,
    pendingDepositsCount: Int,
    pendingWithdrawalsCount: Int,
    unreadNotificationsCount: Int = 0
) {
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFF0B1322),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminSection.values().forEach { section ->
                val isSelected = selectedSection == section
                val badgeCount = when (section) {
                    AdminSection.NOTIFICATIONS -> unreadNotificationsCount
                    AdminSection.DEPOSITS -> pendingDepositsCount
                    AdminSection.WITHDRAWALS -> pendingWithdrawalsCount
                    else -> 0
                }

                val icon = when (section) {
                    AdminSection.DASHBOARD -> Icons.Default.Dashboard
                    AdminSection.NOTIFICATIONS -> Icons.Default.Notifications
                    AdminSection.USERS -> Icons.Default.People
                    AdminSection.INVESTMENTS -> Icons.Default.TrendingUp
                    AdminSection.DEPOSITS -> Icons.Default.AccountBalanceWallet
                    AdminSection.PAYMENT_ACCOUNTS -> Icons.Default.AccountBalance
                    AdminSection.WITHDRAWALS -> Icons.Default.MonetizationOn
                    AdminSection.TRANSACTIONS -> Icons.Default.History
                    AdminSection.AUDIT_LOGS -> Icons.Default.Security
                    AdminSection.SETTINGS -> Icons.Default.Settings
                }

                Surface(
                    onClick = { onSectionSelected(section) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CpiGold else Color(0xFF1E293B),
                    border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF334155), Color(0xFF1E293B))
                        )
                    ),
                    modifier = Modifier.testTag("nav_admin_${section.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) CpiNavy else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = section.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CpiNavy else Color.White
                        )
                        if (badgeCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFEF4444),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "$badgeCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. Dashboard Overview Section
@Composable
private fun AdminDashboardOverviewSection(
    stats: AdminOverviewStats,
    pendingDeposits: List<TransactionEntity>,
    pendingWithdrawals: List<TransactionEntity>,
    recentAuditLogs: List<AuditLogEntity>,
    onNavigateSection: (AdminSection) -> Unit,
    onRefresh: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Platform Operational Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Live telemetry & verified database records",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier.testTag("admin_dashboard_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CpiGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 8 Real Database Statistics Cards (Clickable)
        // Row 1: Users
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClickableAdminMetricCard(
                title = "Total Users",
                value = "${stats.totalRegisteredUsers}",
                subtitle = "Registered accounts",
                icon = Icons.Default.People,
                color = Color(0xFF3B82F6),
                onClick = { onNavigateSection(AdminSection.USERS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_total_users"
            )
            ClickableAdminMetricCard(
                title = "Active Users",
                value = "${stats.activeUsersCount}",
                subtitle = "Active investors",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF10B981),
                onClick = { onNavigateSection(AdminSection.USERS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_active_users"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Deposits
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClickableAdminMetricCard(
                title = "Total Approved Deposits",
                value = "$%,.2f".format(stats.totalApprovedDepositsAmount),
                subtitle = "${stats.totalApprovedDepositsCount} settled records",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF34D399),
                onClick = { onNavigateSection(AdminSection.DEPOSITS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_approved_deposits"
            )
            ClickableAdminMetricCard(
                title = "Pending Deposits",
                value = "${stats.pendingDepositsCount}",
                subtitle = "Awaiting verification",
                icon = Icons.Default.Warning,
                color = Color(0xFFF59E0B),
                onClick = { onNavigateSection(AdminSection.DEPOSITS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_pending_deposits"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 3: Investments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClickableAdminMetricCard(
                title = "Total Investments",
                value = "${stats.totalInvestmentsCount}",
                subtitle = "Positions opened",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF818CF8),
                onClick = { onNavigateSection(AdminSection.INVESTMENTS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_total_investments"
            )
            ClickableAdminMetricCard(
                title = "Active Investments",
                value = "${stats.activeInvestmentsCount}",
                subtitle = "Earning yield",
                icon = Icons.Default.TrendingUp,
                color = CpiGold,
                onClick = { onNavigateSection(AdminSection.INVESTMENTS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_active_investments"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 4: Withdrawals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ClickableAdminMetricCard(
                title = "Pending Withdrawals",
                value = "${stats.pendingWithdrawalsCount}",
                subtitle = "Awaiting authorization",
                icon = Icons.Default.MonetizationOn,
                color = Color(0xFFEF4444),
                onClick = { onNavigateSection(AdminSection.WITHDRAWALS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_pending_withdrawals"
            )
            ClickableAdminMetricCard(
                title = "Completed Withdrawals",
                value = "${stats.completedWithdrawalsCount}",
                subtitle = "Dispatched payouts",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF059669),
                onClick = { onNavigateSection(AdminSection.WITHDRAWALS) },
                modifier = Modifier.weight(1f),
                testTag = "admin_stat_completed_withdrawals"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Queue: Pending Review
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Action Queue (${stats.pendingDepositsCount + stats.pendingWithdrawalsCount} items)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pendingDeposits.isEmpty() && pendingWithdrawals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All pending transactions are clear",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    if (pendingDeposits.isNotEmpty()) {
                        Text(
                            text = "DEPOSIT VERIFICATION (${pendingDeposits.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        pendingDeposits.take(3).forEach { deposit ->
                            PendingItemRow(
                                title = "Deposit #${deposit.id.takeLast(6)}",
                                subtitle = "User: ${deposit.userId} • Ref: ${deposit.reference.ifEmpty { "N/A" }}",
                                amount = "${deposit.amount} ${deposit.currency}",
                                onClick = { onNavigateSection(AdminSection.DEPOSITS) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (pendingWithdrawals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "WITHDRAWAL REQUESTS (${pendingWithdrawals.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        pendingWithdrawals.take(3).forEach { w ->
                            PendingItemRow(
                                title = "Withdrawal #${w.id.takeLast(6)}",
                                subtitle = "User: ${w.userId}",
                                amount = "${w.amount} ${w.currency}",
                                onClick = { onNavigateSection(AdminSection.WITHDRAWALS) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Audit Log Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = CpiGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Security Audit Logs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    TextButton(onClick = { onNavigateSection(AdminSection.AUDIT_LOGS) }) {
                        Text("View All (${stats.totalAuditLogsCount})", color = CpiGold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (recentAuditLogs.isEmpty()) {
                    Text(
                        text = "No administrative events logged yet.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recentAuditLogs.forEach { log ->
                        AuditLogRow(log = log)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

// 7. Settings & Audit Trail Section
@Composable
private fun AdminSettingsAuditSection(
    adminUser: UserEntity,
    auditLogs: List<AuditLogEntity>,
    onLogAction: (String, String, String, String, String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Platform Settings & Cryptographic Governance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Official platform parameters, investment thresholds, and cryptographic audit records",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Platform Configuration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CpiGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "General Platform Configuration",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SecurityPolicyRow(label = "Platform Entity", status = "Crest Point Investment (CPI)")
                SecurityPolicyRow(label = "Supported Currencies", status = "USDT (Crypto), PHP (Fiat)")
                SecurityPolicyRow(label = "System Operational State", status = "ACTIVE — Fully Operational")
                SecurityPolicyRow(label = "Support Contact", status = "support@crestpoint.com")
                SecurityPolicyRow(label = "Compliance Desk", status = "compliance@crestpoint.com")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Published Investment Limits Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(CpiGold.copy(alpha = 0.5f), Color(0xFF1E293B))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = CpiGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Published Investment & Transaction Limits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // USDT Limit Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B132B), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "USDT Portfolio Limits",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Networks: TRC20 & BEP20",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        SecurityPolicyRow(label = "Minimum Deposit / Allocation", status = "50 USDT")
                        SecurityPolicyRow(label = "Maximum Portfolio Allocation", status = "5,000 USDT")
                        SecurityPolicyRow(label = "USDT Supported Networks", status = "TRC20 (Tron) & BEP20 (BSC)")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // PHP Limit Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B132B), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PHP Portfolio Limits",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "Payment: GCash, Maya, Bank Transfer",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        SecurityPolicyRow(label = "Minimum Deposit / Allocation", status = "₱3,000")
                        SecurityPolicyRow(label = "Maximum Portfolio Allocation", status = "₱100,000")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF334155), Color(0xFF1E293B))
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Security & Governance Policies",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SecurityPolicyRow(
                    label = "Role-Based Access Control (RBAC)",
                    status = "ENFORCED (Role-Protected Routes)"
                )
                SecurityPolicyRow(
                    label = "Password Storage",
                    status = "SHA-256 + Unique Salt per user"
                )
                SecurityPolicyRow(
                    label = "Financial Record Integrity",
                    status = "Immutable Ledger + Audited Mutations"
                )
                SecurityPolicyRow(
                    label = "Public Admin Registration",
                    status = "DISABLED (Strict Authorization Only)"
                )
                SecurityPolicyRow(
                    label = "Active Administrator Session",
                    status = "${adminUser.email} (UID: ${adminUser.id})"
                )
            }
        }
    }
}

// Reusable UI Components for Admin

@Composable
private fun SecurityPolicyRow(label: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF94A3B8))
        Text(text = status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
    }
}

@Composable
private fun ClickableAdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(color.copy(alpha = 0.5f), Color(0xFF1E293B))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "View →",
                    fontSize = 10.sp,
                    color = CpiGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PendingItemRow(
    title: String,
    subtitle: String,
    amount: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF334155), Color(0xFF1E293B))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
            Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CpiGold)
        }
    }
}

@Composable
private fun AuditLogRow(log: AuditLogEntity) {
    val dateStr = remember(log.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF334155), Color(0xFF1E293B))
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFD97706),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.action,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.targetType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Text(text = dateStr, fontSize = 10.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Admin: ${log.adminEmail} • ${log.valueChange}",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            if (log.reason.isNotBlank()) {
                Text(
                    text = "Reason: ${log.reason}",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

// Access Denied Screen Component
@Composable
private fun AccessDeniedView(
    title: String,
    message: String,
    buttonText: String,
    onAction: () -> Unit,
    showLogout: Boolean = false,
    onLogout: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090E17))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .testTag("admin_access_denied_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D2E)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFDC2626).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFFDC2626), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CpiGold,
                        contentColor = CpiNavy
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("access_denied_action_button")
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (showLogout) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF87171)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("access_denied_logout")
                    ) {
                        Text("Sign Out", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// ADMIN NOTIFICATIONS SECTION
// ----------------------------------------------------
@Composable
private fun AdminNotificationsSection(
    adminUser: UserEntity,
    notifications: List<NotificationEntity>,
    unreadCount: Int,
    onMarkAsRead: (String) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onNavigateToSection: (AdminSection) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(notifications, selectedFilter, searchQuery) {
        notifications.filter { item ->
            val matchesFilter = when (selectedFilter) {
                "UNREAD" -> !item.isRead
                "DEPOSIT" -> item.type == "DEPOSIT"
                "WITHDRAWAL" -> item.type == "WITHDRAWAL"
                "INVESTMENT" -> item.type == "INVESTMENT"
                "SECURITY" -> item.type == "SECURITY"
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.message.contains(searchQuery, ignoreCase = true) ||
                    (item.relatedId?.contains(searchQuery, ignoreCase = true) == true) ||
                    (item.userId?.contains(searchQuery, ignoreCase = true) == true)

            matchesFilter && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_notifications_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = CpiGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "System & User Notifications",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = Color(0xFFEF4444),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(
                                                text = "$unreadCount UNREAD",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Real-time alerts for investor deposits, withdrawals, and system operations",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        if (unreadCount > 0) {
                            OutlinedButton(
                                onClick = onMarkAllAsRead,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CpiGold),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("admin_notifications_mark_all_read")
                            ) {
                                Text("Mark All Read", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search input
                    androidx.compose.material3.OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by reference, user ID, or keyword...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_notifications_search_field"),
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CpiGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All (${notifications.size})",
                            "UNREAD" to "Unread ($unreadCount)",
                            "DEPOSIT" to "Deposits",
                            "WITHDRAWAL" to "Withdrawals",
                            "INVESTMENT" to "Investments",
                            "SECURITY" to "Security"
                        ).forEach { (key, label) ->
                            val isSelected = selectedFilter == key
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CpiGold,
                                    selectedLabelColor = CpiNavy,
                                    containerColor = Color(0xFF0F172A),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) CpiGold else Color(0xFF334155),
                                    selectedBorderColor = CpiGold
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Notification Items
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("admin_notifications_empty_state"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No notifications found.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real-time alerts will appear here as users initiate transactions or perform account actions.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(items = filteredList, key = { it.notificationId }) { item ->
                AdminNotificationCard(
                    item = item,
                    onMarkAsRead = { onMarkAsRead(item.notificationId) },
                    onActionClick = {
                        when (item.type) {
                            "DEPOSIT" -> onNavigateToSection(AdminSection.DEPOSITS)
                            "WITHDRAWAL" -> onNavigateToSection(AdminSection.WITHDRAWALS)
                            "INVESTMENT" -> onNavigateToSection(AdminSection.INVESTMENTS)
                            else -> onNavigateToSection(AdminSection.TRANSACTIONS)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminNotificationCard(
    item: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onActionClick: () -> Unit
) {
    val typeColor = when (item.type) {
        "DEPOSIT" -> Color(0xFF10B981)
        "WITHDRAWAL" -> Color(0xFFF59E0B)
        "INVESTMENT" -> Color(0xFF38BDF8)
        "SECURITY" -> Color(0xFFEC4899)
        else -> Color(0xFF818CF8)
    }

    val timeFormatted = remember(item.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(item.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.isRead) onMarkAsRead()
            }
            .testTag("admin_notification_item_${item.notificationId}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) Color(0xFF1E293B) else Color(0xFF192233)
        ),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (item.isRead) listOf(Color(0xFF334155), Color(0xFF1E293B))
                else listOf(typeColor.copy(alpha = 0.6f), Color(0xFF334155))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = typeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.type,
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.userId != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "User: ${item.userId}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CpiGold)
                        )
                    }
                }

                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 14.sp,
                fontWeight = if (item.isRead) FontWeight.SemiBold else FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.message,
                fontSize = 12.sp,
                color = if (item.isRead) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                lineHeight = 17.sp
            )

            if (item.relatedId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ref: ${item.relatedId}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )

                    Text(
                        text = "Go to Review →",
                        fontSize = 11.sp,
                        color = CpiGold,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (!item.isRead) onMarkAsRead()
                                onActionClick()
                            }
                            .testTag("admin_notification_action_${item.notificationId}")
                    )
                }
            }
        }
    }
}
