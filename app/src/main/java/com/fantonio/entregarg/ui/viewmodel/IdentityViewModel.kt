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
import com.google.mlkit.vision.common.InputImage
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

    var isProcessingImage by mutableStateOf(false)
        private set

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

    fun processScannedImage(image: android.media.Image, rotationDegrees: Int, onComplete: () -> Unit) {
        val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        
        isProcessingImage = true
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                viewModelScope.launch {
                    val allLines = visionText.textBlocks
                        .flatMap { it.lines }
                        .sortedBy { it.boundingBox?.top ?: 0 } // Ordena visualmente de cima para baixo
                    
                    var startProcessing = false
                    val linesToProcess = mutableListOf<String>()
                    val dateRegex = Regex("\\d{2}/\\d{2}/\\d{4}")
                    
                    for (line in allLines) {
                        val text = line.text.trim()
                        
                        // Verifica se é a linha de cabeçalho para começar
                        if (!startProcessing && 
                            text.contains("RG", ignoreCase = true) && 
                            text.contains("LOTE", ignoreCase = true) && 
                            text.contains("PESSOA", ignoreCase = true)) {
                            startProcessing = true
                            continue // Pula o próprio cabeçalho
                        }
                        
                        // Verifica se encontrou uma data no formato dd/mm/yyyy para parar
                        if (startProcessing && dateRegex.containsMatchIn(text)) {
                            break
                        }
                        
                        if (startProcessing) {
                            linesToProcess.add(text)
                        }
                    }

                    val newIdentities = mutableListOf<Identity>()
                    val cpfRegex = Regex("\\d{3}[.\\s]?\\d{3}[.\\s]?\\d{3}[-\\s]?\\d{2}")
                    
                    // Processamento das linhas filtradas
                    linesToProcess.forEach { line ->
                        val cpfMatch = cpfRegex.find(line)
                        if (cpfMatch != null) {
                            val cpf = cpfMatch.value.replace(Regex("[.\\-\\s]"), "")
                            
                            // Tenta extrair o lote (geralmente um número ou código curto na mesma linha ou próxima)
                            // Se o CPF está na linha, o resto pode ser Nome e Lote
                            val textWithoutCpf = line.replace(cpfMatch.value, "").trim()
                            
                            // Heurística: se houver um número isolado, pode ser o lote
                            val parts = textWithoutCpf.split(Regex("\\s+"))
                            val lote = parts.find { it.all { char -> char.isLetterOrDigit() } && it.length <= 5 } ?: "S/L"
                            val nome = parts.filter { it != lote }.joinToString(" ").trim()

                            if (nome.length > 2) {
                                newIdentities.add(Identity(nome = nome, cpf = cpf, lote = lote))
                            }
                        }
                    }

                    // Se não encontrou nada estruturado, tenta a busca global apenas na área permitida
                    if (newIdentities.isEmpty() && linesToProcess.isNotEmpty()) {
                        val combinedText = linesToProcess.joinToString(" ")
                        cpfRegex.findAll(combinedText).forEach { match ->
                            val cpf = match.value.replace(Regex("[.\\-\\s]"), "")
                            newIdentities.add(Identity(nome = "Detectado em Lote", cpf = cpf, lote = "S/L"))
                        }
                    }

                    // Verifica duplicatas
                    val existingCpfs = repository.getAll().map { it.cpf }.toSet()
                    scannedResults = newIdentities.distinctBy { it.cpf }.map { 
                        ScannedIdentity(it, existingCpfs.contains(it.cpf))
                    }
                    
                    isProcessingImage = false
                    onComplete()
                }
            }
            .addOnFailureListener {
                isProcessingImage = false
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
