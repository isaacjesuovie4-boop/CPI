package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ContactSupportSection
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

// -------------------------------------------------------------
// 1. ABOUT SCREEN (/about)
// -------------------------------------------------------------
@Composable
fun AboutScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("about_screen")
    ) {
        PageHeader(title = "About CPI", subtitle = "Crest Point Investment Platform", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "Platform Overview",
            body = "Crest Point Investment (CPI) is a dedicated financial management platform built to provide individuals and institutions with a consolidated environment for tracking cryptocurrency and stock-market investment portfolios. We focus on transparency, operational reliability, and comprehensive activity records."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Our Core Principles",
            body = "1. Auditable Data: Maintaining clean, persistent records of user activity.\n2. Risk Transparency: Clearly conveying that investment performance varies across markets.\n3. Zero Deceptive Claims: We do not promise guaranteed returns or fabricated yields.\n4. Dual-Currency Adaptability: Supporting USDT (digital stablecoin) and PHP (regional fiat) with dedicated boundaries."
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Investment Governance",
            body = "CPI provides users with tools to manage investments and monitor their active positions. All decisions remain in user hands, and users are encouraged to review full platform risk disclosures before funding their accounts."
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onNavigate("register") },
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("REGISTER HERE", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 2. HOW IT WORKS SCREEN (/how-it-works)
// -------------------------------------------------------------
@Composable
fun HowItWorksScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("how_it_works_page")
    ) {
        PageHeader(title = "How It Works", subtitle = "4 Simple Steps to Begin", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        StepCard(
            stepNumber = "1",
            title = "Create an account",
            description = "Complete our simple registration form with your name, contact details, occupation, and choose between USDT and PHP.",
            icon = Icons.Default.PersonAdd
        )
        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "2",
            title = "Choose an investment",
            description = "Browse available crypto and stock market investment tracks to select the portfolio allocation aligned with your goals.",
            icon = Icons.Default.ShowChart
        )
        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "3",
            title = "Fund your investment",
            description = "Transfer funds via supported crypto channels (USDT TRC20/BEP20, limits 50–5,000 USDT) or regional currency (PHP, limits ₱3,000–₱100,000).",
            icon = Icons.Default.AccountBalanceWallet
        )
        Spacer(modifier = Modifier.height(10.dp))

        StepCard(
            stepNumber = "4",
            title = "Monitor your investment and request withdrawals when eligible",
            description = "Track performance metrics directly on your Investor Dashboard and submit verified withdrawal requests when eligible.",
            icon = Icons.Default.MonetizationOn
        )

        Spacer(modifier = Modifier.height(24.dp))

        SupportedCurrenciesSection()

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onNavigate("register") },
            colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("REGISTER HERE", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 3. FAQ SCREEN (/faq)
// -------------------------------------------------------------
@Composable
fun FaqScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("faq_screen")
    ) {
        PageHeader(title = "Frequently Asked Questions", subtitle = "Common inquiries & guidelines", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        FaqAccordionItem(
            question = "What is Crest Point Investment (CPI)?",
            answer = "CPI is a modern investment management platform that allows users to monitor and manage cryptocurrency and stock-market investments from a single account."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqAccordionItem(
            question = "What currencies and limits are supported?",
            answer = "CPI supports two independent currency options:\n• USDT: 50 USDT minimum, 5,000 USDT maximum (TRC20 & BEP20)\n• PHP: ₱3,000 minimum, ₱100,000 maximum\nThese limits are completely separate and not interchangeable."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqAccordionItem(
            question = "Are investment returns guaranteed?",
            answer = "No. In accordance with strict regulatory and ethical standards, CPI makes no claims of guaranteed profits or guaranteed returns. Investment values fluctuate based on market dynamics."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqAccordionItem(
            question = "How do I request withdrawals?",
            answer = "Withdrawals are submitted directly from your Investor Dashboard once your account reaches the eligible holding milestone for your chosen investment category."
        )

        Spacer(modifier = Modifier.height(10.dp))

        FaqAccordionItem(
            question = "How do I contact client support?",
            answer = "You can reach CPI representatives directly via Telegram (@LOUISA_WILFRED) or WhatsApp (+1 408 333 4636). Official CPI email will be available soon."
        )
    }
}

// -------------------------------------------------------------
// 4. CONTACT SCREEN (/contact)
// -------------------------------------------------------------
@Composable
fun ContactScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("contact_screen")
    ) {
        PageHeader(title = "Contact CPI", subtitle = "Direct support & communication", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        ContactSupportSection()

        Spacer(modifier = Modifier.height(20.dp))

        InfoCard(
            title = "Support Hours",
            body = "Our representative desk operates 24/7 across multiple time zones to assist with account registration, currency questions, and navigation support."
        )
    }
}

// -------------------------------------------------------------
// 5. TERMS OF SERVICE (/terms)
// -------------------------------------------------------------
@Composable
fun TermsScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("terms_screen")
    ) {
        PageHeader(title = "Terms of Service", subtitle = "Legal agreement & user covenants", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "1. Acceptance of Terms",
            body = "By registering an account with Crest Point Investment (CPI), you agree to comply with and be bound by these Terms of Service. If you do not agree, please do not use the platform."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "2. Eligibility & Account Creation",
            body = "Users must provide accurate, current, and complete registration details. Only one account per individual is allowed. You are responsible for maintaining the confidentiality of your account credentials."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "3. Financial Operations & Auditing",
            body = "All transactions, deposits, and withdrawal requests are recorded in persistent, auditable ledgers. CPI does not manipulate or erase historical account activity."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "4. Currency Selection & Network Rules",
            body = "Users must adhere strictly to the separate USDT (50–5,000 USDT on TRC20/BEP20) and PHP (₱3,000–₱100,000) boundaries. Transfers sent on unsupported networks are at user risk."
        )
    }
}

// -------------------------------------------------------------
// 6. PRIVACY POLICY (/privacy)
// -------------------------------------------------------------
@Composable
fun PrivacyScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("privacy_screen")
    ) {
        PageHeader(title = "Privacy Policy", subtitle = "Information collection & data security", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        InfoCard(
            title = "1. Information We Collect",
            body = "We collect information provided during registration: Full Name, Email Address, Phone Number, Country of Residence, Occupation, and Selected Currency Preference."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "2. Password & Credential Security",
            body = "Passwords are never stored in plain text. All credentials are cryptographically hashed using salted SHA-256 algorithms before persistence."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "3. Use of Personal Data",
            body = "Your information is used strictly to identify your account, personalize your investor dashboard, process authorized requests, and ensure platform security."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "4. Third-Party Sharing",
            body = "CPI does not sell, rent, or trade your personal information to third parties. Data is only disclosed where required by lawful regulatory processes."
        )
    }
}

// -------------------------------------------------------------
// 7. RISK DISCLOSURE SCREEN (/risk-disclosure)
// -------------------------------------------------------------
@Composable
fun RiskDisclosureScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SleekBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("risk_disclosure_screen")
    ) {
        PageHeader(title = "Risk Disclosure", subtitle = "Important notice on market & capital risk", onBack = { onNavigate("home") })

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant),
            border = BorderStroke(1.dp, SleekCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "NO GUARANTEED RETURNS NOTICE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crest Point Investment does not guarantee returns, fixed profits, or risk-free yields on any investment option. All capital is subject to market variance.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Market Volatility",
            body = "Digital assets such as cryptocurrencies and equity markets experience significant price fluctuations. Past performance of any asset is not indicative of future results."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "Independent Due Diligence",
            body = "Users should conduct their own independent research and assess their risk tolerance before committing financial resources. CPI provides portfolio tracking tools, not individualized financial advice."
        )

        Spacer(modifier = Modifier.height(10.dp))

        InfoCard(
            title = "Capital Allocation Responsibility",
            body = "Investors should only allocate capital that they can afford to expose to market variance. Ensure you understand all terms and network fee structures before depositing."
        )
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES
// -------------------------------------------------------------
@Composable
private fun PageHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("page_back_button")
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = SleekTextPrimary
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SleekTextSecondary
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SleekTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                color = SleekTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
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
private fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
            .testTag("faq_item_${question.take(10).replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(
            1.dp,
            if (expanded) SleekPrimaryBorder else SleekCardBorder
        )
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
                Text(
                    text = question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (expanded) SleekPrimary else SleekTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (expanded) SleekPrimary else SleekTextMuted
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = SleekCardBorder)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

