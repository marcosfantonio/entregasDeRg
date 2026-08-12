package com.fantonio.entregarg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import coil.compose.AsyncImage
import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.ui.viewmodel.JobViewModel

/**
 * Detalhe de um job. Ao entrar na tela, dispara a chamada que faz o
 * middleware consultar a API pública de câmbio e converter o custo p/ USD.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(job: PrintJob, viewModel: JobViewModel, onEdit: () -> Unit) {
    LaunchedEffect(job.id) {
        viewModel.fetchPriceInUsd(job.id)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(job.itemName) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagem baixada e manipulada (escala de cinza via ColorFilter)
            AsyncImage(
                model = job.imageUrl,
                contentDescription = job.itemName,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text("Cliente: ${job.clientName}")
            Text("Material: ${job.material}")
            Text("Peso: ${job.weightGrams} g")
            Text("Horas de impressão: ${job.printHours} h")
            Text("Status: ${job.status}")
            Text("Custo: R$ ${"%.2f".format(job.costBrl)}")

            val price = viewModel.priceInfo
            if (price != null && price.id == job.id) {
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Custo em USD (via API pública de câmbio):", style = MaterialTheme.typography.labelMedium)
                        Text("US$ ${"%.2f".format(price.costUsd)}", style = MaterialTheme.typography.titleLarge)
                        Text("Cotação usada: 1 BRL = ${"%.4f".format(price.exchangeRate)} USD")
                    }
                }
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Text("Editar job")
            }
        }
    }
}
