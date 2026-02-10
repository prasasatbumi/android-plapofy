package com.finprov.plapofy.presentation.pin.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.finprov.plapofy.R // Assuming resources exist, if not I'll use text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace

@Composable
fun PinInput(
    pinLength: Int = 6,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 32.dp)
        ) {
            repeat(pinLength) { index ->
                val isFilled = index < value.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            if (isFilled) MaterialTheme.colorScheme.primary else Color.Gray,
                            CircleShape
                        )
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Keypad
        PinKeypad(
            onKeyClick = { key ->
                if (value.length < pinLength) {
                    onValueChange(value + key)
                }
            },
            onDeleteClick = {
                if (value.isNotEmpty()) {
                    onValueChange(value.dropLast(1))
                }
            }
        )
    }
}

@Composable
fun PinKeypad(
    onKeyClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
    ) {
        // Rows 1-3
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { key ->
                    PinKey(
                        text = key,
                        onClick = { onKeyClick(key) }
                    )
                }
            }
        }

        // Row 4 (Empty, 0, Delete)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Empty spacer for alignment
            Box(modifier = Modifier.size(64.dp))
            
            PinKey(
                text = "0",
                onClick = { onKeyClick("0") }
            )
            
            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PinKey(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
