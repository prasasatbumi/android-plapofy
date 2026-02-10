package com.finprov.plapofy.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.finprov.plapofy.presentation.auth.AuthViewModel
import com.finprov.plapofy.presentation.auth.ForgotPasswordScreen
import com.finprov.plapofy.presentation.auth.LoginScreen
import com.finprov.plapofy.presentation.auth.RegisterScreen
import com.finprov.plapofy.presentation.home.HomeScreen
import com.finprov.plapofy.presentation.kyc.KycScreen
import com.finprov.plapofy.presentation.loan.ApplyLoanScreen
import com.finprov.plapofy.presentation.loan.LoanDetailScreen
import com.finprov.plapofy.presentation.loan.MyLoansScreen
import com.finprov.plapofy.presentation.loan.apply.ApplyCreditScreen
import com.finprov.plapofy.presentation.loan.dashboard.CreditDashboardScreen
import com.finprov.plapofy.presentation.loan.detail.CreditLineDetailScreen
import com.finprov.plapofy.presentation.loan.disburse.DisburseScreen
import com.finprov.plapofy.presentation.main.BottomNavigationBar
import com.finprov.plapofy.presentation.main.CreditCardTabScreen
import com.finprov.plapofy.presentation.main.shouldShowBottomBar
import com.finprov.plapofy.presentation.profile.ChangePasswordScreen
import com.finprov.plapofy.presentation.profile.ProfileScreen
import com.finprov.plapofy.presentation.simulation.SimulationScreen
import com.finprov.plapofy.presentation.splash.SplashScreen

@Composable
fun PlapofyNavGraph(
    navController: NavHostController,
    deepLinkPath: String? = null,
    onDeepLinkHandled: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Handle deep links
    LaunchedEffect(deepLinkPath) {
        if (deepLinkPath != null) {
            navController.navigate(deepLinkPath) {
                launchSingleTop = true
            }
            onDeepLinkHandled()
        }
    }
    
    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable("splash") {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
        
        // Home Screen (Guest accessible)
        composable("home") {
            HomeScreen(
                onSimulateClick = { plafondId ->
                    navController.navigate("simulate/$plafondId")
                },
                onLoginClick = {
                    navController.navigate("login")
                },
                onProfileClick = {
                    navController.navigate("profile")
                },
                onMyLoansClick = {
                    navController.navigate("my-loans")
                },
                onCreditDashboardClick = {
                    navController.navigate("credit-dashboard")
                },
                onApplyCreditClick = { plafondId ->
                    navController.navigate("apply-credit/$plafondId")
                }
            )
        }
        
        // Simulation Screen (Guest accessible)
        composable(
            route = "simulate/{plafondId}",
            arguments = listOf(navArgument("plafondId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plafondId = backStackEntry.arguments?.getLong("plafondId") ?: 0L
            SimulationScreen(
                plafondId = plafondId,
                onBackClick = { navController.popBackStack() },
                onApplyClick = { amount, tenor ->
                    navController.navigate("apply/$plafondId?amount=${amount.toLong()}&tenor=$tenor")
                }
            )
        }
        
        // Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot-password")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot Password Screen (Email Input)
        composable("forgot-password") {
            ForgotPasswordScreen(
                onNavigateToResetPassword = { email ->
                    navController.navigate("reset-password/$email")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Reset Password Screen (OTP + New Password)
        composable(
            route = "reset-password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            com.finprov.plapofy.presentation.auth.ResetPasswordScreen(
                email = email,
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        // Register Screen
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Profile Screen (Auth required)
        composable("profile") {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onKycClick = {
                    navController.navigate("kyc")
                },
                onHistoryClick = {
                    navController.navigate("my-loans")
                },
                onChangePasswordClick = {
                    navController.navigate("change-password")
                },
                onPinClick = { hasPin ->
                    if (hasPin) {
                        navController.navigate("change-pin")
                    } else {
                        navController.navigate("set-pin")
                    }
                }
            )
        }
        
        // Change Password Screen
        composable("change-password") {
            ChangePasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        
        // Set PIN Screen
        composable("set-pin") {
            com.finprov.plapofy.presentation.pin.SetPinScreen(
                navController = navController,
                onPinSet = {
                    navController.popBackStack()
                }
            )
        }
        
        // Change PIN Screen
        composable("change-pin") {
            com.finprov.plapofy.presentation.pin.ChangePinScreen(
                navController = navController,
                onPinChanged = {
                    navController.popBackStack()
                }
            )
        }
        
        // Apply Loan Screen (Auth required)
        composable(
            route = "apply/{plafondId}?amount={amount}&tenor={tenor}",
            arguments = listOf(
                navArgument("plafondId") { type = NavType.LongType },
                navArgument("amount") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("tenor") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val plafondId = backStackEntry.arguments?.getLong("plafondId") ?: 0L
            val amount = backStackEntry.arguments?.getString("amount")
            val tenor = backStackEntry.arguments?.getString("tenor")

            ApplyLoanScreen(
                plafondId = plafondId,
                initialAmount = amount,
                initialTenor = tenor,
                onBackClick = { navController.popBackStack() },
                onCompleteProfile = {
                    navController.navigate("profile")
                },
                onVerifyKyc = {
                    navController.navigate("kyc")
                },
                onLoginRequired = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onSuccess = {
                    navController.navigate("my-loans") {
                        popUpTo("home")
                    }
                }
            )
        }
        
        // My Loans Screen (Auth required)
        composable("my-loans") {
            MyLoansScreen(
                onBackClick = { navController.popBackStack() },
                onLoanClick = { loanId ->
                    // Negative ID means CreditLine, positive means Loan
                    if (loanId < 0) {
                        // Use absolute value for credit line ID
                        navController.navigate("credit-line-detail/${-loanId}")
                    } else {
                        navController.navigate("loan-detail/$loanId")
                    }
                }
            )
        }
        
        // Loan Detail Screen (from deep link or my-loans)
        composable(
            route = "loan-detail/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loanId = backStackEntry.arguments?.getLong("loanId") ?: 0L
            LoanDetailScreen(
                loanId = loanId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Credit Line Detail Screen (for pending applications with status timeline)
        composable(
            route = "credit-line-detail/{creditLineId}",
            arguments = listOf(navArgument("creditLineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val creditLineId = backStackEntry.arguments?.getLong("creditLineId") ?: 0L
            CreditLineDetailScreen(
                creditLineId = creditLineId,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // KYC Screen
        composable("kyc") {
            KycScreen(
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }
        
        // Apply Credit Screen (Apply for credit limit)
        composable(
            route = "apply-credit/{plafondId}",
            arguments = listOf(navArgument("plafondId") { type = NavType.LongType })
        ) { backStackEntry ->
            val plafondId = backStackEntry.arguments?.getLong("plafondId") ?: 0L
            ApplyCreditScreen(
                plafondId = plafondId,
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate("my-loans") {
                        popUpTo("home")
                    }
                },
                onGoToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        
        // Credit Dashboard Screen (View credit line details)
        composable("credit-dashboard") {
            CreditDashboardScreen(
                onBackClick = { navController.popBackStack() },
                onDisburseClick = { creditLineId ->
                    navController.navigate("disburse")
                }
            )
        }
        
        // Credit Card Tab Screen (Bottom nav - shows credit status)
        composable("credit-card-tab") {
            CreditCardTabScreen(
                onApplyClick = {
                    // Navigate to home to select plafond
                    navController.navigate("home")
                },
                onDetailClick = {
                    navController.navigate("credit-dashboard")
                },
                onDisburseClick = { creditLineId ->
                    navController.navigate("disburse")
                }
            )
        }
        
        // Disburse Screen (Request fund disbursement)
        composable("disburse") {
            DisburseScreen(
                onBackClick = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate("credit-dashboard") {
                        popUpTo("credit-dashboard") { inclusive = true }
                    }
                }
            )
        }
        }
    }
}

