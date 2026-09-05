package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.DashboardRepository
import com.example.ui.AdminSection
import com.example.ui.AdminViewModel
import com.example.ui.AdminViewModelFactory
import com.example.ui.AuthViewModel
import com.example.ui.AuthViewModelFactory
import com.example.ui.DashboardViewModel
import com.example.ui.DashboardViewModelFactory
import com.example.ui.components.CpiDrawer
import com.example.ui.components.CpiTopAppBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.ContactScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FaqScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowItWorksScreen
import com.example.ui.screens.InvestScreen
import com.example.ui.screens.InvestmentsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.RiskDisclosureScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.screens.WithdrawScreen
import com.example.ui.screens.WithdrawalsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val authRepository = AuthRepository(database.userDao())
        val dashboardRepository = DashboardRepository(
            investmentDao = database.investmentDao(),
            transactionDao = database.transactionDao(),
            paymentAccountDao = database.paymentAccountDao(),
            withdrawalDao = database.withdrawalDao(),
            notificationDao = database.notificationDao()
        )
        val adminRepository = AdminRepository(
            userDao = database.userDao(),
            investmentDao = database.investmentDao(),
            transactionDao = database.transactionDao(),
            auditLogDao = database.auditLogDao(),
            paymentAccountDao = database.paymentAccountDao(),
            withdrawalDao = database.withdrawalDao(),
            notificationDao = database.notificationDao()
        )

        val authViewModelFactory = AuthViewModelFactory(authRepository)
        val dashboardViewModelFactory = DashboardViewModelFactory(dashboardRepository)
        val adminViewModelFactory = AdminViewModelFactory(adminRepository)

        setContent {
            MyApplicationTheme {
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardViewModelFactory)
                val adminViewModel: AdminViewModel = viewModel(factory = adminViewModelFactory)
                CpiApp(
                    authViewModel = authViewModel,
                    dashboardViewModel = dashboardViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
fun CpiApp(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    adminViewModel: AdminViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val currentUser by authViewModel.currentUser.collectAsState()
    val unreadNotificationCount by dashboardViewModel.unreadNotificationCount.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CpiDrawer(
                currentRoute = currentRoute,
                currentUser = currentUser,
                unreadNotificationCount = unreadNotificationCount,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CpiTopAppBar(
                    currentUser = currentUser,
                    unreadNotificationCount = unreadNotificationCount,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenDrawer = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("dashboard") {
                    DashboardScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("invest") {
                    InvestScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("investments") {
                    InvestmentsScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("withdraw") {
                    WithdrawScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("withdrawals") {
                    WithdrawalsScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("notifications") {
                    NotificationsScreen(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("about") {
                    AboutScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("how-it-works") {
                    HowItWorksScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("faq") {
                    FaqScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("contact") {
                    ContactScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("terms") {
                    TermsScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("privacy") {
                    PrivacyScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("risk-disclosure") {
                    RiskDisclosureScreen(
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/login") {
                    AdminLoginScreen(
                        authViewModel = authViewModel,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.DASHBOARD,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/users") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.USERS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/investments") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.INVESTMENTS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/deposits") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.DEPOSITS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/payment-accounts") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.PAYMENT_ACCOUNTS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/withdrawals") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.WITHDRAWALS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/transactions") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.TRANSACTIONS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/settings") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.SETTINGS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("admin/notifications") {
                    AdminDashboardScreen(
                        authViewModel = authViewModel,
                        adminViewModel = adminViewModel,
                        initialSection = AdminSection.NOTIFICATIONS,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
