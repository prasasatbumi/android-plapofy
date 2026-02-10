package com.finprov.plapofy.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finprov.plapofy.domain.model.CreditLine
import com.finprov.plapofy.presentation.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActiveCreditCard(
    creditLine: CreditLine,
    onClick: () -> Unit
) {
    val isActive = creditLine.status == com.finprov.plapofy.domain.model.CreditLineStatus.ACTIVE
    val isPending = creditLine.status == com.finprov.plapofy.domain.model.CreditLineStatus.APPLIED || 
                    creditLine.status == com.finprov.plapofy.domain.model.CreditLineStatus.REVIEWED ||
                    creditLine.status == com.finprov.plapofy.domain.model.CreditLineStatus.APPROVED

    val usagePercentage = if (creditLine.approvedLimit > 0) {
        (creditLine.availableBalance / creditLine.approvedLimit).toFloat()
    } else 0f

    val statusText = when(creditLine.status) {
        com.finprov.plapofy.domain.model.CreditLineStatus.APPLIED -> "Menunggu Review"
        com.finprov.plapofy.domain.model.CreditLineStatus.REVIEWED -> "Dalam Proses"
        com.finprov.plapofy.domain.model.CreditLineStatus.APPROVED -> "Disetujui"
        com.finprov.plapofy.domain.model.CreditLineStatus.ACTIVE -> "Aktif"
        com.finprov.plapofy.domain.model.CreditLineStatus.REJECTED -> "Ditolak"
        else -> "Tidak Aktif"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isActive)
                        Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))
                    else
                        Brush.linearGradient(colors = listOf(Color(0xFF757575), Color(0xFF616161)))
                )
                .padding(20.dp)
        ) {
            Column {
                // Header: Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plafond $statusText",
                            style = MaterialTheme.typography.titleMedium,
                            color = White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Tier Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = creditLine.tier,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Balance Section
                if (isActive) {
                    Text(
                        text = "Sisa Limit Tersedia",
                        style = MaterialTheme.typography.labelMedium,
                        color = White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrencyValue(creditLine.availableBalance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = "dari ${formatCurrencyValue(creditLine.approvedLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = White.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Pengajuan Limit",
                        style = MaterialTheme.typography.labelMedium,
                        color = White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrencyValue(creditLine.requestedAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning // Yellowish tint for visibility? Or just White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Bar (Only if active)
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(White.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(usagePercentage.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(White)
                        )
                    }
                    Text(
                        text = "${(usagePercentage * 100).toInt()}% tersisa",
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    // Placeholder for spacing
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Berlaku: ${formatDate(creditLine.validUntil)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    // For pending, maybe show submitted date if available? Or nothing.
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Disburse Button - only enabled if ACTIVE and balance > 0
                    Button(
                        onClick = onClick,
                        enabled = isActive && creditLine.availableBalance > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Primary,
                            disabledContainerColor = White.copy(alpha = 0.5f),
                            disabledContentColor = Primary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cairkan", fontWeight = FontWeight.SemiBold)
                    }
                    
                    // Detail Button
                    OutlinedButton(
                        onClick = onClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(White, White))
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Detail", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun formatCurrencyValue(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount)
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString.take(10)
    } catch (e: Exception) {
        dateString.take(10)
    }
}

