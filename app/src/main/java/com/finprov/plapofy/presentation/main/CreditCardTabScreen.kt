package com.finprov.plapofy.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditLineStatus
import com.finprov.plapofy.presentation.loan.dashboard.CreditDashboardViewModel
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardTabScreen(
    onApplyClick: () -> Unit,
    onDetailClick: () -> Unit,
    onDisburseClick: (Long) -> Unit,
    viewModel: CreditDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Kartu Kredit", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                state.creditLine != null -> {
                    // Has credit line - show status
                    ActiveCreditContent(
                        creditLine = state.creditLine!!,
                        onDetailClick = onDetailClick,
                        onDisburseClick = { onDisburseClick(state.creditLine!!.id) }
                    )
                }
                else -> {
                    // No credit line - show inactive state
                    InactiveCreditContent(onApplyClick = onApplyClick)
                }
            }
        }
    }
}

@Composable
private fun ActiveCreditContent(
    creditLine: CreditLine,
    onDetailClick: () -> Unit,
    onDisburseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Credit Card Visual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PrimaryDark, Primary, Teal)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PLAPOFY",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        StatusBadge(creditLine.status)
                    }
                    
                    Column {
                        Text(
                            text = "Sisa Limit Tersedia",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatCurrency(creditLine.availableBalance),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Limit", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text(
                                text = formatCurrency(creditLine.approvedLimit),
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tier", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Text(
                                text = creditLine.tier.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        if (creditLine.status == CreditLineStatus.ACTIVE || 
            creditLine.status == CreditLineStatus.APPROVED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Lihat Detail")
                }
                Button(
                    onClick = onDisburseClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Cairkan Dana")
                }
            }
        } else {
            // Pending status - just show detail button
            Button(
                onClick = onDetailClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Lihat Detail Pengajuan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Informasi Limit", fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                InfoRowItem("Produk", creditLine.plafondName ?: "-")
                InfoRowItem("Bunga", "${creditLine.interestRate}% / tahun")
                InfoRowItem("Status", getStatusText(creditLine.status))
                creditLine.validUntil?.let {
                    InfoRowItem("Berlaku Hingga", it.take(10))
                }
            }
        }
        
        // Rejection Reason Alert
        if (creditLine.status == CreditLineStatus.REJECTED) {
            Spacer(modifier = Modifier.height(16.dp))
            val rejectionRemarks = creditLine.history
                .filter { it.statusAfter == "REJECTED" }
                .firstOrNull()?.remarks

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Pengajuan Ditolak",
                            fontWeight = FontWeight.Bold,
                            color = Error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rejectionRemarks ?: "Tidak ada alasan yang diberikan",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InactiveCreditContent(onApplyClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Inactive card visual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CreditCardOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = TextHint
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak Ada Kartu Aktif",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Anda belum memiliki limit kredit aktif",
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Ajukan sekarang dan dapatkan limit hingga Rp 300.000.000",
            color = TextHint,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onApplyClick,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajukan Limit Kredit")
        }
    }
}

@Composable
private fun StatusBadge(status: CreditLineStatus) {
    val (bgColor, text) = when (status) {
        CreditLineStatus.APPLIED -> Warning.copy(alpha = 0.2f) to "Diajukan"
        CreditLineStatus.REVIEWED -> Warning.copy(alpha = 0.2f) to "Direview"
        CreditLineStatus.APPROVED -> Success.copy(alpha = 0.2f) to "Disetujui"
        CreditLineStatus.ACTIVE -> Success.copy(alpha = 0.2f) to "Aktif"
        CreditLineStatus.REJECTED -> Error.copy(alpha = 0.2f) to "Ditolak"
        else -> TextHint.copy(alpha = 0.2f) to status.value
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

private fun getStatusText(status: CreditLineStatus): String {
    return when (status) {
        CreditLineStatus.APPLIED -> "Diajukan"
        CreditLineStatus.REVIEWED -> "Sedang Direview"
        CreditLineStatus.APPROVED -> "Disetujui"
        CreditLineStatus.ACTIVE -> "Aktif"
        CreditLineStatus.REJECTED -> "Ditolak"
        CreditLineStatus.EXPIRED -> "Kadaluarsa"
        else -> status.value
    }
}
