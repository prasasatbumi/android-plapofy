package com.finprov.plapofy.presentation.loan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun LoanDetailScreen(
    loanId: Long,
    onBackClick: () -> Unit,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val state by viewModel.myLoansState.collectAsState()
    val loan = state.loans.find { it.id == loanId }
    
    LaunchedEffect(loanId) {
        if (state.loans.isEmpty()) {
            viewModel.loadMyLoans()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pinjaman", fontWeight = FontWeight.Bold) },
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
                .background(Background)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                }
                loan == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextHint
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pinjaman tidak ditemukan",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Kembali")
                        }
                    }
                }
                else -> {
                    LoanDetailContent(loan = loan)
                }
            }
        }
    }
}

@Composable
private fun LoanDetailContent(loan: Loan) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card with Main Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loan.plafondName ?: "Pinjaman",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    StatusChip(status = loan.status)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Jumlah Pinjaman",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = formatCurrency(loan.amount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Surface)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Key Details Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    DetailColumn("Tenor", "${loan.tenor} bulan")
                    DetailColumn("Bunga", "${loan.interestRate}%")
                    DetailColumn("Cicilan", formatCurrency(loan.monthlyInstallment))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loan Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Informasi Pinjaman",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                
                InfoRow("ID Pinjaman", "#${loan.id}")
                loan.branchName?.let { InfoRow("Cabang", it) }
                loan.purpose?.let { InfoRow("Tujuan", it) }
                loan.createdAt?.let { InfoRow("Tanggal Pengajuan", formatDateTime(it)) }
                InfoRow("Total Pembayaran", formatCurrency(loan.totalPayment))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Journey Timeline
        if (loan.approvals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Riwayat Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    loan.approvals.forEachIndexed { index, approval ->
                        TimelineItem(
                            approval = approval,
                            isLast = index == loan.approvals.lastIndex
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Installment Schedule Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jadwal Cicilan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Bulan",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Pokok",
                        modifier = Modifier.weight(1.2f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Bunga",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Total",
                        modifier = Modifier.weight(1.2f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.End
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calculate installment breakdown
                val principalPerMonth = loan.amount / loan.tenor
                val interestPerMonth = loan.monthlyInstallment - principalPerMonth
                
                // Show first 6 months or all if tenor <= 6
                val monthsToShow = minOf(loan.tenor, 6)
                
                for (month in 1..monthsToShow) {
                    InstallmentRow(
                        month = month,
                        principal = principalPerMonth,
                        interest = interestPerMonth,
                        total = loan.monthlyInstallment
                    )
                    if (month < monthsToShow) {
                        Divider(color = Surface.copy(alpha = 0.5f))
                    }
                }
                
                if (loan.tenor > 6) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "... dan ${loan.tenor - 6} bulan lainnya",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextHint
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Surface)
                Spacer(modifier = Modifier.height(12.dp))
                
                // Total Summary
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total ${loan.tenor} Bulan",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = formatCurrency(loan.totalPayment),
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun InstallmentRow(
    month: Int,
    principal: Double,
    interest: Double,
    total: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = "$month",
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = TextPrimary
        )
        Text(
            text = formatShortCurrency(principal),
            modifier = Modifier.weight(1.2f),
            fontSize = 13.sp,
            color = TextPrimary,
            textAlign = TextAlign.End
        )
        Text(
            text = formatShortCurrency(interest),
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = Warning,
            textAlign = TextAlign.End
        )
        Text(
            text = formatShortCurrency(total),
            modifier = Modifier.weight(1.2f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Primary,
            textAlign = TextAlign.End
        )
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

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ")
}

private fun formatShortCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fJt", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.0fRb", amount / 1_000)
        else -> formatCurrency(amount)
    }
}

@Composable
private fun TimelineItem(approval: com.finprov.plapofy.domain.model.LoanApproval, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Timeline Indicator
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            // Dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        when (approval.statusAfter) {
                            LoanStatus.APPROVED, LoanStatus.DISBURSED, LoanStatus.PAID -> Success
                            LoanStatus.REJECTED, LoanStatus.CANCELLED -> Error
                            else -> Primary
                        }
                    )
            )
            // Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(TextHint.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 24.dp)) {
            Text(
                text = approval.statusAfter.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = formatDateTime(approval.timestamp ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            
            // Hide remarks for REJECTED status as requested
            if (!approval.remarks.isNullOrBlank() && approval.statusAfter != com.finprov.plapofy.domain.model.LoanStatus.REJECTED) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${approval.remarks}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = TextSecondary
                )
            }
        }
    }
}

private fun formatDateTime(isoString: String): String {
    if (isoString.isBlank()) return "-"
    return try {
        // Simple regex parsing or use SimpleDateFormat. 
        // Backend usually sends ISO 8601 ex: 2026-01-28T10:00:00Z
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = formatter.parse(isoString.substringBefore(".")) // Handle potentially variable fractional seconds
        val printFormat = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        printFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoString // Fallback
    }
}
