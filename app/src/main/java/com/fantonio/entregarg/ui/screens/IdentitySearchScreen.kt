package com.fantonio.entregarg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
fun IdentityCard(
    identity: Identity,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = identity.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "CPF: ${identity.cpf}", fontSize = 14.sp)
            Text(text = "Lote: ${identity.lote}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (identity.retirada) {
                val dateStr = identity.retiradaData?.let {
                    SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                } ?: ""
                Text(
                    text = "RETIRADA por: ${identity.retiradaPor}",
                    color = Color(0xFF388E3C), // Green
                    fontWeight = FontWeight.Medium
                )
                Text(text = "Em: $dateStr", fontSize = 12.sp, color = Color.Gray)
            } else {
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
