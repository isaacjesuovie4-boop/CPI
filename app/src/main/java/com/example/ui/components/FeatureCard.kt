package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

data class CpiFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val tag: String
)

val cpiFeaturesList = listOf(
    CpiFeature(
        title = "Investment Tracking",
        description = "Monitor your cryptocurrency and stock portfolio performance with clean, consolidated metrics.",
        icon = Icons.Default.Timeline,
        iconTint = SleekPrimary,
        tag = "feature_investment_tracking"
    ),
    CpiFeature(
        title = "Account Management",
        description = "Easily update profile settings, manage chosen currency preferences, and review active status.",
        icon = Icons.Default.AccountCircle,
        iconTint = SleekPrimary,
        tag = "feature_account_management"
    ),
    CpiFeature(
        title = "Deposit Management",
        description = "Clear funding allocations supporting USDT (TRC20/BEP20) and PHP via streamlined channels.",
        icon = Icons.Default.AccountBalanceWallet,
        iconTint = SleekPrimary,
        tag = "feature_deposit_management"
    ),
    CpiFeature(
        title = "Withdrawal Requests",
        description = "Structured withdrawal workflows allowing investors to submit and track requests when eligible.",
        icon = Icons.Default.Payments,
        iconTint = SleekPrimary,
        tag = "feature_withdrawal_requests"
    ),
    CpiFeature(
        title = "Transaction History",
        description = "Persistent, auditable activity logs recording every account action, deposit, and withdrawal step.",
        icon = Icons.Default.History,
        iconTint = SleekPrimary,
        tag = "feature_transaction_history"
    ),
    CpiFeature(
        title = "Investor Dashboard",
        description = "A centralized portal tailored to investor needs with responsive views across mobile, tablet, and desktop.",
        icon = Icons.Default.Dashboard,
        iconTint = SleekPrimary,
        tag = "feature_investor_dashboard"
    ),
    CpiFeature(
        title = "Customer Support",
        description = "Dedicated direct channels on Telegram and WhatsApp ready to assist with account and technical inquiries.",
        icon = Icons.Default.SupportAgent,
        iconTint = SleekPrimary,
        tag = "feature_customer_support"
    )
)

@Composable
fun FeaturesSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("features_section")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SleekPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CORE CAPABILITIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SleekPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Engineered for Clarity & Control",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Every tool you need to manage your personal investment accounts from a unified interface.",
            fontSize = 13.sp,
            color = SleekTextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        cpiFeaturesList.forEach { feature ->
            FeatureItemCard(feature = feature)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun FeatureItemCard(feature: CpiFeature, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(feature.tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = feature.description,
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

