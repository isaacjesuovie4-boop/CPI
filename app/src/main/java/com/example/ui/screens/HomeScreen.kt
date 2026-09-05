package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.ContactSupportSection
import com.example.ui.components.FeaturesSection
import com.example.ui.components.SupportedCurrenciesSection
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

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
            .testTag("home_screen")
    ) {
        // Hero Section
        HeroSection(onNavigate = onNavigate)

        Spacer(modifier = Modifier.height(28.dp))

        // Main content padding container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // About CPI Section
            AboutCpiSection(onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(32.dp))

            // How It Works Section (4 Steps)
            HowItWorksSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Supported Currencies (USDT & PHP separate)
            SupportedCurrenciesSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Core Features (7 cards)
            FeaturesSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Transparency & Risk Disclosure Section
            TransparencySection(onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(32.dp))

            // Contact Channels (Telegram & WhatsApp)
            ContactSupportSection()

            Spacer(modifier = Modifier.height(36.dp))

            // Footer Navigation Links
            FooterSection(onNavigate = onNavigate)
        }
    }
}

@Composable
fun HeroSection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SleekPrimaryContainer.copy(alpha = 0.5f),
                        SleekBg
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("hero_section")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tag badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekPrimaryContainer)
                    .border(1.dp, SleekPrimaryBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SleekPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECURE INVESTMENT MANAGEMENT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SleekPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headline
            Text(
                text = "Invest Smarter.\nTrack Your Progress.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = "Crest Point Investment provides a convenient platform for managing and monitoring cryptocurrency and stock-market investments from one account.",
                fontSize = 14.sp,
                color = SleekTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Graphic Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SleekSurface),
                border = BorderStroke(1.dp, SleekCardBorder)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_finance),
                    contentDescription = "CPI Finance Platform Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate("register") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("hero_register_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REGISTER HERE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = { onNavigate("login") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SleekPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SleekPrimaryBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hero_login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LOGIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AboutCpiSection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("about_cpi_section"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SleekPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ABOUT CREST POINT INVESTMENT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = SleekPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Dedicated to Structured Portfolio Oversight",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crest Point Investment (CPI) provides users with a comprehensive platform for managing investments and monitoring their investment activity across diversified digital asset classes and regional markets.",
                fontSize = 13.sp,
                color = SleekTextSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Our platform is built upon transparent reporting, secure accounting principles, and straightforward access. We empower investors with complete visibility into their account positions and transaction records.",
                fontSize = 13.sp,
                color = SleekTextSecondary,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .clickable { onNavigate("about") }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Read more about CPI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HowItWorksSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("how_it_works_section")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SleekPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FOUR-STEP PROCESS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = SleekPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "How It Works",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Get started with Crest Point Investment through our streamlined 4-step workflow:",
            fontSize = 13.sp,
            color = SleekTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        StepCard(
            stepNumber = "1",
            title = "Create an account",
            description = "Register your profile with your details, set up a secure password, and select your preferred currency (USDT or PHP).",
            icon = Icons.Default.PersonAdd
        )

        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "2",
            title = "Choose an investment",
            description = "Explore available cryptocurrency and stock-market investment options tailored to your portfolio strategy.",
            icon = Icons.Default.ShowChart
        )

        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "3",
            title = "Fund your investment",
            description = "Deposit funds securely using USDT (via TRC20 or BEP20 network) or PHP within approved platform limits.",
            icon = Icons.Default.AccountBalanceWallet
        )

        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "4",
            title = "Monitor your investment and request withdrawals when eligible",
            description = "Track real-time performance on your Investor Dashboard and submit withdrawal requests according to platform eligibility schedules.",
            icon = Icons.Default.MonetizationOn
        )
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SleekPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SleekPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun TransparencySection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transparency_section"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRANSPARENCY & RISK DISCLOSURE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = SleekPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Investment Performance & Capital Risk Notice",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Investment performance may vary based on market conditions, asset selection, and macroeconomic factors. Crest Point Investment does not make claims of guaranteed profits or guaranteed returns.",
                fontSize = 12.sp,
                color = SleekTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Users are strongly advised to review the platform's Terms of Service, Privacy Policy, and Risk Disclosure documentation before making financial commitments. All records maintained in subsequent stages are auditable and immutable.",
                fontSize = 12.sp,
                color = SleekTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate("risk-disclosure") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SleekPrimaryBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("Risk Disclosure", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { onNavigate("terms") },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekPrimary),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SleekPrimaryBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("Terms of Service", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun FooterSection(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("footer_section")
    ) {
        HorizontalDivider(color = SleekCardBorder)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Crest Point Investment (CPI)",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = SleekTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "A modern, reliable foundation for multi-asset management.",
            fontSize = 12.sp,
            color = SleekTextMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Navigation Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FooterLink("Home", onClick = { onNavigate("home") })
                FooterLink("About CPI", onClick = { onNavigate("about") })
                FooterLink("How It Works", onClick = { onNavigate("how-it-works") })
                FooterLink("FAQ", onClick = { onNavigate("faq") })
            }
            Column(modifier = Modifier.weight(1f)) {
                FooterLink("Contact Support", onClick = { onNavigate("contact") })
                FooterLink("Risk Disclosure", onClick = { onNavigate("risk-disclosure") })
                FooterLink("Terms of Service", onClick = { onNavigate("terms") })
                FooterLink("Privacy Policy", onClick = { onNavigate("privacy") })
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "© 2026 Crest Point Investment. All rights reserved.",
            fontSize = 11.sp,
            color = SleekTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FooterLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = SleekPrimary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

