package com.fantonio.entregarg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.data.model.PrintJobRequest
import com.fantonio.entregarg.ui.viewmodel.JobViewModel

/**
 * Tela usada tanto para CRIAR (POST) quanto para EDITAR (PUT) um job.
 * Se [existingJob] for null, é um cadastro novo; caso contrário, edição.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFormScreen(
    viewModel: JobViewModel,
    existingJob: PrintJob?,
    onSaved: () -> Unit
) {
    var clientName by remember { mutableStateOf(existingJob?.clientName ?: "") }
    var itemName by remember { mutableStateOf(existingJob?.itemName ?: "") }
    var material by remember { mutableStateOf(existingJob?.material ?: "PLA") }
    var weight by remember { mutableStateOf(existingJob?.weightGrams?.toString() ?: "") }
    var hours by remember { mutableStateOf(existingJob?.printHours?.toString() ?: "") }
    var cost by remember { mutableStateOf(existingJob?.costBrl?.toString() ?: "") }
    var status by remember { mutableStateOf(existingJob?.status ?: "queued") }
    var imageUrl by remember { mutableStateOf(existingJob?.imageUrl ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (existingJob == null) "Novo job" else "Editar job") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(clientName, { clientName = it }, label = { Text("Cliente") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(itemName, { itemName = it }, label = { Text("Item / peça") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(material, { material = it }, label = { Text("Material (PLA/PETG/ABS)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                weight, { weight = it }, label = { Text("Peso (g)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                hours, { hours = it }, label = { Text("Horas de impressão") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                cost, { cost = it }, label = { Text("Custo (R\$)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(status, { status = it }, label = { Text("Status") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                imageUrl, { imageUrl = it },
                label = { Text("URL da foto (opcional - vazio = imagem automática)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val request = PrintJobRequest(
                        clientName = clientName,
                        itemName = itemName,
                        material = material,
                        weightGrams = weight.toDoubleOrNull() ?: 0.0,
                        printHours = hours.toDoubleOrNull() ?: 0.0,
                        costBrl = cost.toDoubleOrNull() ?: 0.0,
                        status = status,
                        imageUrl = imageUrl.ifBlank { null }
                    )
                    if (existingJob == null) {
                        viewModel.createJob(request) { onSaved() }
                    } else {
                        viewModel.updateJob(existingJob.id, request) { onSaved() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existingJob == null) "Criar job (POST)" else "Salvar alterações (PUT)")
            }
        }
    }
}
