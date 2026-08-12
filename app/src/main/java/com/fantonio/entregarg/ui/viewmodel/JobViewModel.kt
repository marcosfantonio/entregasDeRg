package com.fantonio.entregarg.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.data.model.PrintJobPrice
import com.fantonio.entregarg.data.model.PrintJobRequest
import com.fantonio.entregarg.data.repository.JobRepository
import kotlinx.coroutines.launch

class JobViewModel(
    private val repository: JobRepository = JobRepository()
) : ViewModel() {

    var jobs by mutableStateOf<List<PrintJob>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var priceInfo by mutableStateOf<PrintJobPrice?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                jobs = repository.listJobs()
            } catch (e: Exception) {
                errorMessage = "Falha ao carregar jobs: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createJob(job: PrintJobRequest, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.createJob(job)
                refresh()
                onDone()
            } catch (e: Exception) {
                errorMessage = "Falha ao criar job: ${e.message}"
            }
        }
    }

    fun updateJob(id: Int, job: PrintJobRequest, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateJob(id, job)
                refresh()
                onDone()
            } catch (e: Exception) {
                errorMessage = "Falha ao atualizar job: ${e.message}"
            }
        }
    }

    fun deleteJob(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteJob(id)
                refresh()
            } catch (e: Exception) {
                errorMessage = "Falha ao apagar job: ${e.message}"
            }
        }
    }

    fun fetchPriceInUsd(id: Int) {
        viewModelScope.launch {
            try {
                priceInfo = repository.getPriceInUsd(id)
            } catch (e: Exception) {
                errorMessage = "Falha ao consultar câmbio: ${e.message}"
            }
        }
    }

    fun clearPriceInfo() {
        priceInfo = null
    }
}
