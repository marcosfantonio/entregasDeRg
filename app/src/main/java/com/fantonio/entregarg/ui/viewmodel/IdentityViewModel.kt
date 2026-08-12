package com.fantonio.entregarg.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fantonio.entregarg.data.local.AppDatabase
import com.fantonio.entregarg.data.model.Identity
import com.fantonio.entregarg.data.repository.IdentityRepository
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScannedIdentity(
    val identity: Identity,
    val isDuplicate: Boolean
)

data class DetectedText(
    val text: String,
    val boundingBox: android.graphics.Rect
)

data class IdentityStats(
    val total: Int = 0,
    val withdrawn: Int = 0,
    val remaining: Int = 0,
    val withdrawnPercentage: Float = 0f
)

@OptIn(FlowPreview::class)
class IdentityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IdentityRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQueryFlow = _searchQuery.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Identity>>(emptyList())
        private set
    
    var isSearching by mutableStateOf(false)
        private set

    var importStatus by mutableStateOf<String?>(null)
        private set

    var scannedResults by mutableStateOf<List<ScannedIdentity>>(emptyList())
        private set

    var detectedRects by mutableStateOf<List<android.graphics.Rect>>(emptyList())
        private set

    var isProcessingImage by mutableStateOf(false)
        private set

    var stats by mutableStateOf(IdentityStats())
        private set

    private var allIdentities = emptyList<Identity>()

    init {
        val dao = AppDatabase.getDatabase(application).identityDao()
        repository = IdentityRepository(dao)

        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        searchResults = emptyList()
                        isSearching = false
                    } else {
                        isSearching = true
                        searchResults = repository.search(query)
                        isSearching = false
                    }
                }
        }

        // Observe stats and keep internal list updated
        viewModelScope.launch {
            dao.getAllIdentities().collect { list ->
                allIdentities = list
                val total = list.size
                val withdrawn = list.count { it.retirada }
                val remaining = total - withdrawn
                val percentage = if (total > 0) (withdrawn.toFloat() / total) * 100 else 0f
                stats = IdentityStats(total, withdrawn, remaining, percentage)
            }
        }
    }

    fun showWithdrawn() {
        searchQuery = ""
        _searchQuery.value = ""
        searchResults = allIdentities.filter { it.retirada }
    }

    fun showPending() {
        searchQuery = ""
        _searchQuery.value = ""
        searchResults = allIdentities.filter { !it.retirada }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        _searchQuery.value = newQuery
    }

    fun search() {
        // Trigger manual se necessário
        _searchQuery.value = searchQuery
    }

    fun markAsWithdrawn(identity: Identity, withdrawnBy: String) {
        viewModelScope.launch {
            val updated = identity.copy(
                retirada = true,
                retiradaPor = withdrawnBy,
                retiradaData = System.currentTimeMillis()
            )
            repository.update(updated)
            search() // Refresh results
        }
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch {
            importStatus = "Importando..."
            try {
                val identities = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    inputStream?.use { stream ->
                        val reader = InputStreamReader(stream)
                        com.fantonio.entregarg.util.CsvParser.parseIdentities(reader)
                    } ?: emptyList()
                }
                
                if (identities.isNotEmpty()) {
                    repository.insertAll(identities)
                    importStatus = "Sucesso: ${identities.size} registros importados."
                } else {
                    importStatus = "Erro: Nenhum dado válido encontrado no CSV."
                }
            } catch (e: Exception) {
                importStatus = "Erro ao importar: ${e.message}"
            }
        }
    }

    fun clearImportStatus() {
        importStatus = null
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            importStatus = "Exportando..."
            try {
                withContext(Dispatchers.IO) {
                    val identities = repository.getAll()
                    val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
                    outputStream?.use { stream ->
                        val writer = CSVWriter(OutputStreamWriter(stream))
                        
                        // Header
                        writer.writeNext(arrayOf("Nome", "CPF", "Lote", "Retirada", "Data/Hora", "Quem Retirou"))
                        
                        val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                        
                        identities.forEach { identity ->
                            val dateStr = identity.retiradaData?.let { dateFormat.format(Date(it)) } ?: ""
                            writer.writeNext(arrayOf(
                                identity.nome,
                                identity.cpf,
                                identity.lote,
                                if (identity.retirada) "Sim" else "Não",
                                dateStr,
                                identity.retiradaPor ?: ""
                            ))
                        }
                        writer.close()
                    }
                }
                importStatus = "Sucesso: Exportação concluída."
            } catch (e: Exception) {
                importStatus = "Erro ao exportar: ${e.message}"
            }
        }
    }

    fun onTextDetected(visionText: com.google.mlkit.vision.text.Text) {
        detectedRects = visionText.textBlocks.mapNotNull { it.boundingBox }
    }

    fun processScannedImage(visionText: com.google.mlkit.vision.text.Text, onComplete: () -> Unit) {
        viewModelScope.launch {
            val allLines = visionText.textBlocks
                .flatMap { it.lines }
                .sortedBy { it.boundingBox?.top ?: 0 }
            
            var startProcessing = false
            val linesToProcess = mutableListOf<com.google.mlkit.vision.text.Text.Line>()
            val dateRegex = Regex("\\d{2}/\\d{2}/\\d{4}")
            
            // Cabeçalho esperado: RG   Nº Lote   Pessoa
            for (line in allLines) {
                val text = line.text.trim().uppercase()
                
                if (!startProcessing) {
                    val hasRg = text.contains("RG")
                    val hasLote = text.contains("LOTE") || text.contains("LT")
                    val hasPessoa = text.contains("PESSOA") || text.contains("NOME")
                    
                    if (hasRg || (hasLote && hasPessoa)) {
                        startProcessing = true
                        continue
                    }
                }
                
                // Para se encontrar a data de rodapé ou o indicador de página
                if (startProcessing && (dateRegex.containsMatchIn(text) || text.contains("PÁG", ignoreCase = true))) {
                    break
                }
                
                if (startProcessing) {
                    linesToProcess.add(line)
                }
            }

            val newIdentities = mutableListOf<Identity>()
            // RG costuma ter de 7 a 11 dígitos
            val rgRegex = Regex("\\d{7,11}")
            // Lote no exemplo tem 6 dígitos
            val loteRegex = Regex("\\d{6}")
            
            linesToProcess.forEach { line ->
                val text = line.text.trim()
                
                // Tenta encontrar RG e Lote na linha
                val rgMatch = rgRegex.find(text)
                val loteMatch = loteRegex.find(text)
                
                if (rgMatch != null) {
                    val rg = rgMatch.value
                    
                    // Se o lote não estiver na mesma linha de texto detectada, 
                    // procuramos o próximo número de 6 dígitos
                    val lote = loteMatch?.value ?: ""
                    
                    // Extração do Nome: removemos RG e Lote do texto original
                    var nome = text.replace(rg, "").replace(lote, "").trim()
                    
                    // Limpeza de caracteres residuais (como o "Nº" ou símbolos de assinatura)
                    nome = nome.replace(Regex("^[^A-Z]+"), "").trim()
                    
                    // Se o nome vier com a assinatura (muito comum à direita), 
                    // pegamos apenas a parte em maiúsculas que parece ser o nome
                    val nomeParts = nome.split(" ")
                    val cleanNome = nomeParts.takeWhile { part -> 
                        part.length > 1 && part.all { it.isUpperCase() || it == '-' } 
                    }.joinToString(" ")

                    if (cleanNome.length > 3) {
                        newIdentities.add(
                            Identity(
                                nome = cleanNome,
                                cpf = rg,
                                lote = lote.ifEmpty { "S/L" }
                            )
                        )
                    }
                }
            }

            // Fallback: Se não encontrou nada estruturado, tenta busca por blocos
            if (newIdentities.isEmpty()) {
                visionText.textBlocks.forEach { block ->
                    val blockText = block.text.trim()
                    rgRegex.findAll(blockText).forEach { match ->
                        val rg = match.value
                        newIdentities.add(Identity(nome = "Detectado em Bloco", cpf = rg, lote = "S/L"))
                    }
                }
            }

            val existingCpfs = repository.getAll().map { it.cpf }.toSet()
            scannedResults = newIdentities.distinctBy { it.cpf }.map { 
                ScannedIdentity(it, existingCpfs.contains(it.cpf))
            }
            
            isProcessingImage = false
            onComplete()
        }
    }

    fun confirmScannedIdentities() {
        val toAdd = scannedResults.filter { !it.isDuplicate }.map { it.identity }
        if (toAdd.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertAll(toAdd)
                importStatus = "adicionadas ${toAdd.size} identidades com sucessos"
                scannedResults = emptyList()
            }
        }
    }
}
