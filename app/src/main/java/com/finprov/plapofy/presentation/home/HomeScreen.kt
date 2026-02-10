package com.finprov.plapofy.presentation.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.R
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.Plafond
import com.finprov.plapofy.presentation.common.PlapofyButton
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onSimulateClick: (Long) -> Unit,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onMyLoansClick: () -> Unit = {},
    onCreditDashboardClick: () -> Unit = {},
    onApplyCreditClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeCreditLine by viewModel.activeCreditLine.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        viewModel.loadActiveCreditLine()
        viewModel.loadPlafonds()
    }

    Scaffold(
        topBar = {
            HomeHeader(
                isLoggedIn = isLoggedIn,
                onLoginClick = onLoginClick,
                onProfileClick = onProfileClick,
                onMyLoansClick = onMyLoansClick,
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.loadPlafonds() }) {
                            Text("Retry")
                        }
                    }
                }
                is HomeUiState.Success -> {
                    PlafondList(
                        plafonds = state.plafonds,
                        activeCreditLine = activeCreditLine,
                        onSimulate = onSimulateClick,
                        onApplyCredit = onApplyCreditClick,
                        onCreditDashboard = onCreditDashboardClick
                    )
                }
            }
        }
    }
}

@Composable
fun OjkBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Terdaftar & Diawasi oleh OJK",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Text(
                    text = "Layanan pinjaman aman dan terpercaya.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PromoSection() {
    Column {
        PaddingValues(horizontal = 16.dp)
        Text(
            text = "Promo Spesial",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        androidx.compose.foundation.lazy.LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { index ->
                PromoCard(index)
            }
        }
    }
}

@Composable
fun PromoCard(index: Int) {
    val colors = listOf(Color(0xFFE3F2FD), Color(0xFFE8F5E9), Color(0xFFFFF3E0))
    val titles = listOf("Bunga 0% Bulan Pertama", "Cashback Admin 50%", "Referral Bonus 100rb")
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = colors[index % colors.size]),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = titles[index % titles.size],
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Syarat & Ketentuan berlaku",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Default.LocalOffer,
                contentDescription = null,
                tint = Primary.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun HomeHeader(
    isLoggedIn: Boolean,
    onLoginClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMyLoansClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Selamat Datang di",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Plapofy",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
            
            // User Menu
            if (isLoggedIn) {
                Box {
                    IconButton(onClick = { onMenuExpandedChange(true) }) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Menu",
                            tint = White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { onMenuExpandedChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Profil Saya") },
                            onClick = {
                                onMenuExpandedChange(false)
                                onProfileClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Pinjaman Saya") },
                            onClick = {
                                onMenuExpandedChange(false)
                                onMyLoansClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Receipt, null) }
                        )
                    }
                }
            } else {
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = Primary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Masuk", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PlafondList(
    plafonds: List<Plafond>,
    activeCreditLine: CreditLine?,
    onSimulate: (Long) -> Unit,
    onApplyCredit: (Long) -> Unit,
    onCreditDashboard: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OjkBanner()
        }

        // Show Active Credit Card if user has approved credit line
        if (activeCreditLine != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ActiveCreditCard(
                    creditLine = activeCreditLine,
                    onClick = onCreditDashboard
                )
            }
        }

        item {
            PromoSection()
        }

        item {
            Text(
                text = "Produk Pinjaman",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        items(plafonds) { plafond ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PlafondCard(
                    plafond = plafond,
                    hasActiveCreditLine = activeCreditLine != null,
                    onSimulate = onSimulate,
                    onApplyCredit = onApplyCredit
                )
            }
        }
    }
}

@Composable
fun PlafondCard(
    plafond: Plafond,
    hasActiveCreditLine: Boolean,
    onSimulate: (Long) -> Unit,
    onApplyCredit: (Long) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = plafond.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = plafond.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Limit hingga", style = MaterialTheme.typography.labelSmall, color = TextHint)
                    Text(
                        text = formatCurrency(plafond.maxAmount),
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Bunga", style = MaterialTheme.typography.labelSmall, color = TextHint)
                    Text(
                        text = "${plafond.interestRate}% / tahun",
                        style = MaterialTheme.typography.titleSmall,
                        color = Teal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Button changes based on whether user has active credit line
            Spacer(modifier = Modifier.height(16.dp))
            
            // Always show 'Ajukan Limit' to be consistent. 
            // Validation for existing active credit line will be handled in the application flow.
            PlapofyButton(
                text = "Ajukan Limit",
                onClick = { onApplyCredit(plafond.id) }
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

