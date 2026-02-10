package com.finprov.plapofy.presentation.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.domain.model.Loan
import com.finprov.plapofy.domain.model.LoanStatus
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    onBackClick: () -> Unit,
    onLoanClick: (Long) -> Unit = {},
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.myLoansState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyLoans()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pinjaman Saya", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error!!,
                            textAlign = TextAlign.Center,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMyLoans() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                state.loans.isEmpty() -> {
                    EmptyLoansView(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.loans) { loan ->
                            LoanCard(
                                loan = loan,
                                onClick = { onLoanClick(loan.id) },
                                onFinishClick = { id -> viewModel.finishMockLoan(id) }
                            )
                        }
                        
                        item {
                            Button(
                                onClick = { viewModel.resetPlafond() },
                                colors = ButtonDefaults.buttonColors(containerColor = Error),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Reset Plafond (Dev)")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLoansView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextHint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum Ada Pinjaman",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Anda belum memiliki pinjaman.\nAjukan pinjaman pertama Anda!",
            textAlign = TextAlign.Center,
            color = TextSecondary
        )
    }
}

@Composable
private fun LoanCard(
    loan: Loan,
    onClick: () -> Unit,
    onFinishClick: (Long) -> Unit
) {
    val isOffline = loan.createdAt == "Offline Mode"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Product Name + Status or Offline Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loan.plafondName ?: "Pinjaman",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
                
                if (isOffline) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Warning.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Warning)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Warning
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Menunggu Koneksi",
                                color = Warning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    StatusChip(status = loan.status)
                }
            }
            
            if (isOffline) {
                 Spacer(modifier = Modifier.height(8.dp))
                 Text(
                    text = "Permohonan akan dikirim otomatis saat online.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amount
            Text(
                text = formatCurrency(loan.amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Surface)

            Spacer(modifier = Modifier.height(12.dp))

            // Details Grid - only show tenor/cicilan if they have values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (loan.tenor > 0) {
                    LoanDetailItem(
                        label = "Tenor",
                        value = "${loan.tenor} bulan"
                    )
                }
                if (loan.interestRate > 0) {
                    LoanDetailItem(
                        label = "Bunga",
                        value = "${loan.interestRate}%"
                    )
                }
                if (loan.monthlyInstallment > 0) {
                    LoanDetailItem(
                        label = "Cicilan/Bulan",
                        value = formatCurrency(loan.monthlyInstallment)
                    )
                }
            }

            // Purpose if available
            if (!loan.purpose.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tujuan: ${loan.purpose}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Date
            loan.createdAt?.let { date ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diajukan: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextHint
                )
            }
            
            // Mock Finish Button (Dev only)
            if (loan.status == LoanStatus.DISBURSED) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onFinishClick(loan.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text("Selesaikan Pinjaman (Dev)", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: LoanStatus) {
    val (backgroundColor, textColor) = when (status) {
        LoanStatus.DRAFT -> TextHint.copy(alpha = 0.15f) to TextHint
        LoanStatus.SUBMITTED -> Primary.copy(alpha = 0.15f) to Primary
        LoanStatus.PENDING -> Warning.copy(alpha = 0.15f) to Warning
        LoanStatus.REVIEWED -> Primary.copy(alpha = 0.15f) to Primary
        LoanStatus.APPROVED -> Success.copy(alpha = 0.15f) to Success
        LoanStatus.REJECTED -> Error.copy(alpha = 0.15f) to Error
        LoanStatus.DISBURSED -> Primary.copy(alpha = 0.15f) to Primary
        LoanStatus.PAID -> Success.copy(alpha = 0.15f) to Success
        LoanStatus.COMPLETED -> Teal.copy(alpha = 0.15f) to Teal
        LoanStatus.CANCELLED -> Error.copy(alpha = 0.15f) to Error
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoanDetailItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}
