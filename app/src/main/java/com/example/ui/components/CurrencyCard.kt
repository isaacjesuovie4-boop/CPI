package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekOnSecondaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryBorder
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondaryBorder
import com.example.ui.theme.SleekSecondaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary

@Composable
fun SupportedCurrenciesSection(
    modifier: Modifier = Modifier,
    onSelectCurrency: ((String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("supported_currencies_section")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SleekPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SUPPORTED CURRENCIES & LIMITS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = SleekPrimary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Flexible Funding in Crypto & Fiat",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Choose between international crypto assets (USDT) or regional currency (PHP). Limits and networks are fully independent.",
            fontSize = 13.sp,
            color = SleekTextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // USDT Card
        UsdtCurrencyCard()

        Spacer(modifier = Modifier.height(12.dp))

        // PHP Card
        PhpCurrencyCard()

        Spacer(modifier = Modifier.height(12.dp))

        // Compliance notice regarding separation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SleekSurfaceVariant)
                .border(1.dp, SleekCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Currency limits are independent. USDT amounts operate between 50 – 5,000 USDT on TRC20/BEP20. PHP amounts operate strictly between ₱3,000 – ₱100,000.",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun UsdtCurrencyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("currency_card_usdt"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekPrimaryContainer),
        border = BorderStroke(1.dp, SleekPrimaryBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₮",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "USDT (Tether)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SleekOnPrimaryContainer
                        )
                        Text(
                            text = "Digital Dollar Stablecoin",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, SleekPrimaryBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "50 – 5,000 USDT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SleekPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MINIMUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "50 USDT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekOnPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "MAXIMUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "5,000 USDT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekOnPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "NETWORKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Row(modifier = Modifier.padding(top = 2.dp)) {
                        NetworkBadge("TRC20")
                        Spacer(modifier = Modifier.width(4.dp))
                        NetworkBadge("BEP20")
                    }
                }
            }
        }
    }
}

@Composable
fun PhpCurrencyCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("currency_card_php"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSecondaryContainer),
        border = BorderStroke(1.dp, SleekSecondaryBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₱",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekOnSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PHP (Philippine Peso)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SleekOnSecondaryContainer
                        )
                        Text(
                            text = "Direct Regional Fiat",
                            fontSize = 11.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, SleekSecondaryBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "₱3,000 – ₱100,000",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SleekOnSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MINIMUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "₱3,000",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekOnSecondaryContainer
                    )
                }
                Column {
                    Text(
                        text = "MAXIMUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "₱100,000",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SleekOnSecondaryContainer
                    )
                }
                Column {
                    Text(
                        text = "SETTLEMENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Bank / E-Wallet",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SleekOnSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkBadge(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White)
            .border(1.dp, SleekPrimaryBorder.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = SleekPrimary
        )
    }
}

