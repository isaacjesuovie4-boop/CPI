package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.ui.theme.SleekBg
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekError
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun CpiDrawer(
    currentRoute: String,
    currentUser: UserEntity?,
    unreadNotificationCount: Int = 0,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = SleekBg,
        drawerContentColor = SleekTextPrimary,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("cpi_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CpiLogo(iconSize = 32.dp, isDark = false, modifier = Modifier.weight(1f))
                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Menu",
                        tint = SleekTextPrimary
                    )
                }
            }

            // User Profile snippet if logged in
            if (currentUser != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekSurfaceVariant)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Active User",
                                tint = SleekPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentUser.fullName,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Account: ${currentUser.id} • ${currentUser.selectedCurrency}",
                            color = SleekTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = SleekCardBorder
            )

            // Main Nav Items
            DrawerNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == "home",
                testTag = "nav_home",
                onClick = {
                    onNavigate("home")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.Info,
                label = "About CPI",
                isSelected = currentRoute == "about",
                testTag = "nav_about",
                onClick = {
                    onNavigate("about")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.WorkOutline,
                label = "How It Works",
                isSelected = currentRoute == "how-it-works",
                testTag = "nav_how_it_works",
                onClick = {
                    onNavigate("how-it-works")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.HelpOutline,
                label = "FAQ",
                isSelected = currentRoute == "faq",
                testTag = "nav_faq",
                onClick = {
                    onNavigate("faq")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.ContactSupport,
                label = "Contact Support",
                isSelected = currentRoute == "contact",
                testTag = "nav_contact",
                onClick = {
                    onNavigate("contact")
                    onCloseDrawer()
                }
            )

            if (currentUser != null) {
                if (currentUser.role == "ADMIN") {
                    DrawerNavItem(
                        icon = Icons.Default.Lock,
                        label = "Admin Console",
                        isSelected = currentRoute == "admin",
                        testTag = "nav_admin_console",
                        onClick = {
                            onNavigate("admin")
                            onCloseDrawer()
                        }
                    )
                }

                DrawerNavItem(
                    icon = Icons.Default.AccountCircle,
                    label = "Investor Dashboard",
                    isSelected = currentRoute == "dashboard",
                    testTag = "nav_dashboard",
                    onClick = {
                        onNavigate("dashboard")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.Default.Notifications,
                    label = if (unreadNotificationCount > 0) "Notifications ($unreadNotificationCount)" else "Notifications",
                    isSelected = currentRoute == "notifications",
                    testTag = "nav_notifications",
                    onClick = {
                        onNavigate("notifications")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Invest Now",
                    isSelected = currentRoute == "invest",
                    testTag = "nav_invest",
                    onClick = {
                        onNavigate("invest")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.Default.Description,
                    label = "My Investments",
                    isSelected = currentRoute == "investments",
                    testTag = "nav_investments",
                    onClick = {
                        onNavigate("investments")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Request Withdrawal",
                    isSelected = currentRoute == "withdraw",
                    testTag = "nav_withdraw",
                    onClick = {
                        onNavigate("withdraw")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "Withdrawal Records",
                    isSelected = currentRoute == "withdrawals",
                    testTag = "nav_withdrawals",
                    onClick = {
                        onNavigate("withdrawals")
                        onCloseDrawer()
                    }
                )

                DrawerNavItem(
                    icon = Icons.Default.AccountCircle,
                    label = "Account Profile & Settings",
                    isSelected = currentRoute == "profile",
                    testTag = "nav_profile",
                    onClick = {
                        onNavigate("profile")
                        onCloseDrawer()
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = SleekCardBorder
            )

            // Transparency & Policies
            Text(
                text = "TRANSPARENCY & POLICIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            DrawerNavItem(
                icon = Icons.Default.Warning,
                label = "Risk Disclosure",
                isSelected = currentRoute == "risk-disclosure",
                testTag = "nav_risk_disclosure",
                onClick = {
                    onNavigate("risk-disclosure")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.Description,
                label = "Terms of Service",
                isSelected = currentRoute == "terms",
                testTag = "nav_terms",
                onClick = {
                    onNavigate("terms")
                    onCloseDrawer()
                }
            )

            DrawerNavItem(
                icon = Icons.Default.PrivacyTip,
                label = "Privacy Policy",
                isSelected = currentRoute == "privacy",
                testTag = "nav_privacy",
                onClick = {
                    onNavigate("privacy")
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (currentUser == null) {
                    Button(
                        onClick = {
                            onNavigate("register")
                            onCloseDrawer()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("drawer_register_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGISTER HERE",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onNavigate("login")
                            onCloseDrawer()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SleekPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SleekCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("drawer_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOGIN",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            onLogout()
                            onCloseDrawer()
                            onNavigate("home")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SleekError
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, SleekError.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("drawer_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = SleekError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LOGOUT",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer note
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Crest Point Investment © 2026",
                    fontSize = 11.sp,
                    color = SleekTextMuted
                )
                Text(
                    text = "Telegram: @LOUISA_WILFRED",
                    fontSize = 11.sp,
                    color = SleekPrimary
                )
                Text(
                    text = "WhatsApp: +1 408 333 4636",
                    fontSize = 11.sp,
                    color = SleekTextSecondary
                )
            }
        }
    }
}

@Composable
private fun DrawerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) SleekPrimaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) SleekPrimary else SleekTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) SleekOnPrimaryContainer else SleekTextSecondary
            )
        }
    }
}

