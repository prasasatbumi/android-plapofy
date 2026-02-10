package com.finprov.plapofy.presentation.loan.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.domain.model.CreditLineHistory
import com.finprov.plapofy.domain.model.CreditLineStatus
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditLineDetailScreen(
    creditLineId: Long,
    onBackClick: () -> Unit,
    viewModel: CreditLineDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(creditLineId) {
        viewModel.loadCreditLine(creditLineId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pengajuan", fontWeight = FontWeight.Bold) },
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
                        Text(text = state.error!!, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadCreditLine(creditLineId) }) {
                            Text("Coba Lagi")
                        }
                    }
                }
                state.creditLine != null -> {
                    CreditLineDetailContent(state.creditLine!!)
                }
            }
        }
    }
}

@Composable
private fun CreditLineDetailContent(creditLine: CreditLine) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = creditLine.plafondName ?: "Credit Line",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                    StatusChip(creditLine.status)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Jumlah Pengajuan", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text(
                    text = formatCurrency(creditLine.requestedAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                if (creditLine.approvedLimit > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Limit Disetujui", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(
                        text = formatCurrency(creditLine.approvedLimit),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoColumn("Bunga", "${creditLine.interestRate}% / tahun")
                    InfoColumn("Tier", creditLine.tier)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Informasi Pengajuan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoRow("ID Pengajuan", "#${creditLine.id}")
                if (creditLine.branchName != null) {
                    InfoRow("Cabang", creditLine.branchName)
                }
                creditLine.createdAt?.let {
                    InfoRow("Tanggal Pengajuan", formatDate(it))
                }
                creditLine.validUntil?.let {
                    InfoRow("Berlaku Sampai", formatDate(it))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Timeline
        if (creditLine.history.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Riwayat Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StatusTimeline(creditLine.history)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatusTimeline(history: List<CreditLineHistory>) {
    val sortedHistory = history.sortedBy { it.timestamp }
    
    Column {
        sortedHistory.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                // Timeline indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(getStatusColor(item.statusAfter))
                    )
                    if (index < sortedHistory.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(40.dp)
                                .background(Surface)
                        )
                    }
                }
                
                // Content
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = getStatusDisplayName(item.statusAfter),
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = formatDate(item.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    item.remarks?.let { remarks ->
                        Text(
                            text = "\"$remarks\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHint,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (index < sortedHistory.size - 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: CreditLineStatus) {
    val (backgroundColor, textColor) = when (status) {
        CreditLineStatus.APPLIED -> Primary.copy(alpha = 0.15f) to Primary
        CreditLineStatus.REVIEWED -> Warning.copy(alpha = 0.15f) to Warning
        CreditLineStatus.APPROVED -> Success.copy(alpha = 0.15f) to Success
        CreditLineStatus.ACTIVE -> Success.copy(alpha = 0.15f) to Success
        CreditLineStatus.DISBURSED -> Teal.copy(alpha = 0.15f) to Teal
        CreditLineStatus.REJECTED -> Error.copy(alpha = 0.15f) to Error
        CreditLineStatus.EXPIRED -> TextHint.copy(alpha = 0.15f) to TextHint
        CreditLineStatus.UNKNOWN -> TextHint.copy(alpha = 0.15f) to TextHint
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Text(
            text = getStatusDisplayName(status.value),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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

private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate
    } catch (e: Exception) {
        isoDate.take(16).replace("T", " ")
    }
}

private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "APPLIED" -> Primary
        "REVIEWED" -> Warning
        "APPROVED", "ACTIVE" -> Success
        "DISBURSED" -> Teal
        "REJECTED" -> Error
        "EXPIRED" -> TextHint
        else -> TextHint
    }
}

private fun getStatusDisplayName(status: String): String {
    return when (status.uppercase()) {
        "APPLIED" -> "Diajukan"
        "REVIEWED" -> "Sedang Direview"
        "APPROVED" -> "Disetujui"
        "ACTIVE" -> "Aktif"
        "DISBURSED" -> "Dicairkan"
        "REJECTED" -> "Ditolak"
        "EXPIRED" -> "Kadaluarsa"
        else -> status
    }
}
