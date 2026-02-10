package com.finprov.plapofy.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.finprov.plapofy.presentation.theme.*

/**
 * Reusable error state component
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
            text = message,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

/**
 * Reusable empty state component
 */
@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Inbox,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextHint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * Reusable loading overlay
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Memuat...", color = TextSecondary)
                    }
                }
            }
        }
    }
}

/**
 * Success snackbar content
 */
@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Success
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Success,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Warning message component
 */
@Composable
fun WarningMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Warning
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Warning,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
