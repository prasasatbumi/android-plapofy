package com.finprov.plapofy.presentation.loan.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.domain.model.CreditDisbursement
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.TextSecondary
import com.finprov.plapofy.presentation.theme.White
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditDashboardScreen(
    onBackClick: () -> Unit,
    onDisburseClick: (Long) -> Unit, // CreditLineId
    viewModel: CreditDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Kredit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = White,
                    navigationIconContentColor = White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
            
            if (state.error != null) {
                Text(
                    text = state.error!!, 
                    color = MaterialTheme.colorScheme.error, 
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.creditLine == null && !state.isLoading) {
                 Text("Tidak ada kredit aktif", modifier = Modifier.align(Alignment.Center))
            } else if (state.creditLine != null) {
                val creditLine = state.creditLine!!
                
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Balance Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Primary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Sisa Limit Tersedia", color = White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    formatCurrency(creditLine.availableBalance),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Limit", color = White.copy(alpha = 0.6f), fontSize = 12.sp)
                                        Text(formatCurrency(creditLine.approvedLimit), color = White, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Tier", color = White.copy(alpha = 0.6f), fontSize = 12.sp)
                                        Text(creditLine.tier, color = White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        QuickActionRow(
                            onDisburse = { onDisburseClick(creditLine.id) },
                            canDisburse = creditLine.availableBalance > 0,
                            onRefresh = { viewModel.loadData() }
                        )
                    }

                    // Pending Disbursements Section
                    if (state.pendingDisbursements.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Sync,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Menunggu Dikirim (${state.pendingDisbursements.size})",
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    state.pendingDisbursements.forEach { pending ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                formatCurrency(pending.amount),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "${pending.tenor} bulan",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Akan dikirim otomatis saat terhubung ke internet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column {
                            Text(
                                "Riwayat Pencairan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            FilterSection(
                                currentFilter = state.statusFilter,
                                onFilterSelected = { viewModel.setStatusFilter(it) }
                            )
                        }
                    }

                    if (state.filteredDisbursements.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada data", color = Color.Gray)
                            }
                        }
                    } else {
                        items(state.filteredDisbursements.sortedByDescending { it.disbursedAt }) { item ->
                            DisbursementItem(item = item, onPay = { viewModel.mockPayOff(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionRow(
    onDisburse: () -> Unit,
    canDisburse: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onDisburse,
            enabled = canDisburse,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cairkan Dana")
        }
        
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Refresh")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    currentFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Semua", "Aktif", "Lunas").forEach { filter ->
            FilterChip(
                selected = currentFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.1f),
                    selectedLabelColor = Primary
                )
            )
        }
    }
}

@Composable
fun DisbursementItem(item: CreditDisbursement, onPay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.disbursedAt.take(10), style = MaterialTheme.typography.bodySmall, color = TextSecondary) // Simple date substring
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.status == "PAID_OFF") Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(formatCurrency(item.amount), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${item.tenor} Bulan", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (item.status != "PAID_OFF") {
                     OutlinedButton(
                         onClick = onPay,
                         modifier = Modifier.height(32.dp),
                         contentPadding = PaddingValues(horizontal = 8.dp)
                     ) {
                         Text("Bayar", fontSize = 12.sp)
                     }
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}
