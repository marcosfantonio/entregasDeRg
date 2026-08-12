package com.fantonio.entregarg.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.ui.viewmodel.JobViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    viewModel: JobViewModel,
    onAddClick: () -> Unit,
    onJobClick: (PrintJob) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Entrega de RG - Jobs de Impressão") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Novo job")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                viewModel.isLoading && viewModel.jobs.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.errorMessage != null && viewModel.jobs.isEmpty() -> {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.jobs, key = { it.id }) { job ->
                            JobCard(
                                job = job,
                                onClick = { onJobClick(job) },
                                onDelete = { viewModel.deleteJob(job.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobCard(job: PrintJob, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // COIL baixa a imagem da URL (Lorem Picsum ou foto enviada) e aplica
            // uma transformação (cantos arredondados) antes de exibir/cachear.
            AsyncImage(
                model = job.imageUrl,
                contentDescription = job.itemName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onClick() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).clickable { onClick() }) {
                Text(job.itemName, style = MaterialTheme.typography.titleMedium)
                Text("${job.clientName} • ${job.material}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "R$ ${"%.2f".format(job.costBrl)} • ${job.status}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Apagar job")
            }
        }
    }
}
