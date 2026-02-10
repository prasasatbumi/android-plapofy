package com.finprov.plapofy.presentation.simulation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finprov.plapofy.R
import com.finprov.plapofy.presentation.common.PlapofyButton
import com.finprov.plapofy.presentation.home.formatCurrency
import com.finprov.plapofy.presentation.theme.Primary
import com.finprov.plapofy.presentation.theme.PrimaryDark
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationScreen(
    plafondId: Long,
    onBackClick: () -> Unit,
    onApplyClick: (Double, Int) -> Unit,
    viewModel: SimulationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Initialize state with plafond details once loaded
    var amount by remember { mutableStateOf(0.0) }
    var tenor by remember { mutableStateOf(0f) }
    
    // Track if user has manually changed values to avoid overwriting with defaults repeatedly
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(plafondId) {
        viewModel.loadPlafond(plafondId)
    }

    // Set initial values once plafond is loaded
    LaunchedEffect(state.plafond) {
        if (!isInitialized && state.plafond != null) {
            amount = state.plafond!!.minAmount
            tenor = state.plafond!!.minTenor.toFloat()
            isInitialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulasi Pinjaman", fontWeight = FontWeight.Bold) },
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.plafond == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.plafond == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadPlafond(plafondId) }) {
                        Text("Coba Lagi")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Image
                    Image(
                        painter = painterResource(id = R.drawable.calculate),
                        contentDescription = "Calculate",
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    state.plafond?.let { plafond ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Atur Pinjaman Anda",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                // AMOUNT SLIDER SECTION
                                Text(
                                    text = "Jumlah Pinjaman",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formatCurrency(amount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )
                                
                                Slider(
                                    value = amount.toFloat(),
                                    onValueChange = { amount = (it / 100000).roundToInt() * 100000.0 }, // Snap to 100k
                                    valueRange = plafond.minAmount.toFloat()..plafond.maxAmount.toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Primary,
                                        activeTrackColor = Primary,
                                        inactiveTrackColor = Primary.copy(alpha = 0.2f)
                                    )
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatCurrency(plafond.minAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = formatCurrency(plafond.maxAmount),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // TENOR SLIDER SECTION
                                val availableTenors = plafond.interests.sortedBy { it.tenor }
                                var tenorIndex by remember { mutableFloatStateOf(0f) }

                                // Sync state with slider index
                                LaunchedEffect(tenor) {
                                    val index = availableTenors.indexOfFirst { it.tenor.toFloat() == tenor }
                                    if (index != -1) {
                                        tenorIndex = index.toFloat()
                                    }
                                }

                                Text(
                                    text = "Jangka Waktu",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${tenor.toInt()} Bulan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDark
                                )

                                if (availableTenors.isNotEmpty()) {
                                    Slider(
                                        value = tenorIndex,
                                        onValueChange = { 
                                            tenorIndex = it
                                            val index = it.roundToInt().coerceIn(0, availableTenors.lastIndex)
                                            tenor = availableTenors[index].tenor.toFloat()
                                        },
                                        valueRange = 0f..(availableTenors.size - 1).toFloat(),
                                        steps = if (availableTenors.size > 1) availableTenors.size - 2 else 0,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Primary,
                                            activeTrackColor = Primary,
                                            inactiveTrackColor = Primary.copy(alpha = 0.2f)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        availableTenors.forEach { interest ->
                                            Text(
                                                text = "${interest.tenor} Bln",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (interest.tenor.toFloat() == tenor) Primary else Color.Gray,
                                                fontWeight = if (interest.tenor.toFloat() == tenor) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                PlapofyButton(
                                    text = "Hitung Angsuran",
                                    onClick = {
                                        viewModel.simulate(plafond.id, amount, tenor.toInt())
                                    },
                                    enabled = amount > 0 && tenor > 0 && !state.isLoading
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // RESULT SECTION
                    if (state.simulationResult != null) {
                        ResultCard(
                            result = state.simulationResult!!,
                            onApplyClick = {
                                onApplyClick(state.simulationResult!!.amount, state.simulationResult!!.tenor)
                            }
                        )
                    } else if (state.isLoading) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: com.finprov.plapofy.domain.model.LoanSimulation,
    onApplyClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimasi Angsuran Bulanan",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(result.monthlyInstallment),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(color = Primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pokok Pinjaman", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(formatCurrency(result.amount), fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Bunga/Bulan", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("${result.interestRate}%", fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onApplyClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ajukan Pinjaman Ini", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
