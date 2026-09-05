package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationEntity
import com.example.ui.AuthViewModel
import com.example.ui.DashboardViewModel
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val notifications by dashboardViewModel.notifications.collectAsState()
    val unreadCount by dashboardViewModel.unreadNotificationCount.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(currentUser) {
        dashboardViewModel.setUserId(currentUser?.id)
    }

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            onNavigate("login")
        }
        return
    }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "UNREAD" -> notifications.filter { !it.isRead }
            "DEPOSIT" -> notifications.filter { it.type == "DEPOSIT" }
            "WITHDRAWAL" -> notifications.filter { it.type == "WITHDRAWAL" }
            "INVESTMENT" -> notifications.filter { it.type == "INVESTMENT" }
            else -> notifications
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("notifications_screen"),
        color = SleekBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Screen Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onNavigate("dashboard") },
                        modifier = Modifier.testTag("notifications_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = SleekTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Notifications",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            if (unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SleekPrimary)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .testTag("notifications_unread_badge")
                                ) {
                                    Text(
                                        text = "$unreadCount New",
                                        color = Color(0xFF0F172A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Status updates and alerts",
                            fontSize = 12.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                if (unreadCount > 0) {
                    TextButton(
                        onClick = { dashboardViewModel.markAllNotificationsAsRead() },
                        modifier = Modifier.testTag("notifications_mark_all_read_button")
                    )
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilterChip(
                    label = "All (${notifications.size})",
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    testTag = "filter_all"
                )
                NotificationFilterChip(
                    label = "Unread ($unreadCount)",
                    selected = selectedFilter == "UNREAD",
                    onClick = { selectedFilter = "UNREAD" },
                    testTag = "filter_unread"
                )
                NotificationFilterChip(
                    label = "Deposits",
                    selected = selectedFilter == "DEPOSIT",
                    onClick = { selectedFilter = "DEPOSIT" },
                    testTag = "filter_deposit"
                )
                NotificationFilterChip(
                    label = "Withdrawals",
                    selected = selectedFilter == "WITHDRAWAL",
                    onClick = { selectedFilter = "WITHDRAWAL" },
                    testTag = "filter_withdrawal"
                )
                NotificationFilterChip(
                    label = "Investments",
                    selected = selectedFilter == "INVESTMENT",
                    onClick = { selectedFilter = "INVESTMENT" },
                    testTag = "filter_investment"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notifications List or Empty State
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp)
                        .testTag("notifications_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SleekSurface),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = SleekTextMuted,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No notifications yet.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (selectedFilter == "ALL") {
                                    "When your deposits, withdrawals, or investments change status, you will receive real-time updates here."
                                } else {
                                    "No notifications matching the selected filter."
                                },
                                fontSize = 13.sp,
                                color = SleekTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("notifications_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredNotifications,
                        key = { it.notificationId }
                    ) { notification ->
                        NotificationItemCard(
                            notification = notification,
                            onMarkAsRead = {
                                dashboardViewModel.markNotificationAsRead(notification.notificationId)
                            },
                            onNavigateToRelated = {
                                when (notification.type) {
                                    "DEPOSIT" -> onNavigate("dashboard")
                                    "WITHDRAWAL" -> onNavigate("withdrawals")
                                    "INVESTMENT" -> onNavigate("investments")
                                    else -> onNavigate("dashboard")
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
        border = BorderStroke(1.dp, SleekPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.height(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Mark all read",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NotificationFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SleekPrimary,
            selectedLabelColor = Color(0xFF0F172A),
            containerColor = SleekSurface,
            labelColor = SleekTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) SleekPrimary else SleekCardBorder,
            selectedBorderColor = SleekPrimary,
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
private fun NotificationItemCard(
    notification: NotificationEntity,
    onMarkAsRead: () -> Unit,
    onNavigateToRelated: () -> Unit
) {
    val typeConfig = getNotificationTypeConfig(notification.type)
    val timeFormatted = remember(notification.createdAt) {
        formatNotificationTime(notification.createdAt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead) {
                    onMarkAsRead()
                }
            }
            .testTag("notification_item_${notification.notificationId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) SleekSurface else SleekSurfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (notification.isRead) SleekCardBorder else SleekPrimary.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type Icon Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(typeConfig.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeConfig.icon,
                    contentDescription = notification.type,
                    tint = typeConfig.tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            fontSize = 14.sp,
                            fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = SleekTextPrimary
                        )

                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(SleekPrimary)
                            )
                        }
                    }

                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = SleekTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (notification.isRead) SleekTextSecondary else SleekTextPrimary,
                    lineHeight = 18.sp
                )

                if (notification.relatedId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ref: ${notification.relatedId}",
                                fontSize = 10.sp,
                                color = SleekTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "View Details →",
                            fontSize = 11.sp,
                            color = SleekPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                if (!notification.isRead) {
                                    onMarkAsRead()
                                }
                                onNavigateToRelated()
                            }
                        )
                    }
                }
            }
        }
    }
}

private data class NotificationTypeConfig(
    val icon: ImageVector,
    val tintColor: Color,
    val containerColor: Color
)

private fun getNotificationTypeConfig(type: String): NotificationTypeConfig {
    return when (type) {
        "DEPOSIT" -> NotificationTypeConfig(
            icon = Icons.Default.ArrowDownward,
            tintColor = Color(0xFF10B981),
            containerColor = Color(0xFF10B981).copy(alpha = 0.15f)
        )
        "WITHDRAWAL" -> NotificationTypeConfig(
            icon = Icons.Default.ArrowUpward,
            tintColor = Color(0xFFF59E0B),
            containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)
        )
        "INVESTMENT" -> NotificationTypeConfig(
            icon = Icons.Default.TrendingUp,
            tintColor = Color(0xFF38BDF8),
            containerColor = Color(0xFF38BDF8).copy(alpha = 0.15f)
        )
        "SECURITY" -> NotificationTypeConfig(
            icon = Icons.Default.Security,
            tintColor = Color(0xFFEC4899),
            containerColor = Color(0xFFEC4899).copy(alpha = 0.15f)
        )
        else -> NotificationTypeConfig(
            icon = Icons.Default.Info,
            tintColor = Color(0xFF6366F1),
            containerColor = Color(0xFF6366F1).copy(alpha = 0.15f)
        )
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
