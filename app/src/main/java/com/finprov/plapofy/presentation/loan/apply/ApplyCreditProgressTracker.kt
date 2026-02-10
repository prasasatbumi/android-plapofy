package com.finprov.plapofy.presentation.loan.apply

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.TextSecondary
import com.finprov.plapofy.presentation.theme.White

@Composable
fun ApplyCreditProgressTracker(
    currentStep: Int,
    steps: List<String> = listOf("Data Pengajuan", "Dokumen", "Review"),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep
            
            StepItem(
                step = stepNumber,
                title = title,
                isCompleted = isCompleted,
                isCurrent = isCurrent,
                modifier = Modifier.weight(1f)
            )
            
            // Divider line between steps (except last one)
            if (index < steps.size - 1) {
                Divider(
                    color = if (isCompleted) Primary else Color.Gray.copy(alpha = 0.3f),
                    thickness = 2.dp,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(horizontal = 4.dp)
                        .offset(y = (-10).dp) // Align with circle center
                )
            }
        }
    }
}

@Composable
private fun StepItem(
    step: Int,
    title: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle Indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Primary
                        isCurrent -> Primary
                        else -> Color.Gray.copy(alpha = 0.2f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    color = if (isCurrent) White else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent || isCompleted) Primary else TextSecondary,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}
