package com.fantonio.entregarg.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fantonio.entregarg.data.model.Identity
import com.fantonio.entregarg.ui.components.WithdrawDialog
import com.fantonio.entregarg.ui.viewmodel.IdentityViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentitySearchScreen(
    viewModel: IdentityViewModel,
    onSettingsClick: () -> Unit
) {
    var identityToWithdraw by remember { mutableStateOf<Identity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta de Identidades") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Gráfico de Estatísticas
            StatsDashboard(
                stats = viewModel.stats,
                onWithdrawnClick = { viewModel.showWithdrawn() },
                onPendingClick = { viewModel.showPending() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                label = { Text("Nome ou CPF") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { viewModel.search() }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isSearching) {
                CircularProgressIndicator()
            } else if (viewModel.searchResults.isEmpty() && viewModel.searchQuery.isNotBlank()) {
                Text("Nenhuma identidade encontrada.", color = MaterialTheme.colorScheme.error)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.searchResults) { identity ->
                    IdentityCard(
                        identity = identity,
                        onWithdrawClick = { identityToWithdraw = identity }
                    )
                }
            }
        }
    }

    identityToWithdraw?.let { identity ->
        WithdrawDialog(
            onDismiss = { identityToWithdraw = null },
            onConfirm = { name ->
                viewModel.markAsWithdrawn(identity, name)
                identityToWithdraw = null
            }
        )
    }
}

@Composable
fun StatsDashboard(
    stats: com.fantonio.entregarg.ui.viewmodel.IdentityStats,
    onWithdrawnClick: () -> Unit,
    onPendingClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estatísticas de Entrega",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                StatItem(label = "Total:", value = "${stats.total}", color = MaterialTheme.colorScheme.onSurface)
                StatItem(
                    label = "Retiradas:", 
                    value = "${stats.withdrawn}", 
                    color = Color(0xFF388E3C),
                    onClick = onWithdrawnClick
                )
                StatItem(
                    label = "Pendentes:", 
                    value = "${stats.remaining}", 
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onPendingClick
                )
            }

            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                val colorRemaining = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                val colorWithdrawn = Color(0xFF4CAF50)

                Canvas(modifier = Modifier.size(80.dp)) {
                    // Background Circle (Pending)
                    drawArc(
                        color = colorRemaining,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Foreground Circle (Withdrawn)
                    drawArc(
                        color = colorWithdrawn,
                        startAngle = -90f,
                        sweepAngle = 3.6f * stats.withdrawnPercentage,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${stats.withdrawnPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorWithdrawn
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color, onClick: (() -> Unit)? = null) {
    Row(
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun IdentityCard(
    identity: Identity,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (identity.retirada) {
            // Formato Compacto para Entregues
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = identity.nome, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "CPF: ${identity.cpf} | Lote: ${identity.lote}", fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val dateStr = identity.retiradaData?.let {
                        SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                    } ?: ""
                    Text(
                        text = identity.retiradaPor ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF388E3C)
                    )
                    Text(text = dateStr, fontSize = 10.sp, color = Color.Gray)
                }
            }
        } else {
            // Formato Padrão para Não Entregues
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = identity.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "CPF: ${identity.cpf}", fontSize = 14.sp)
                Text(text = "Lote: ${identity.lote}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Marcar Retirada")
                }
            }
        }
    }
}
