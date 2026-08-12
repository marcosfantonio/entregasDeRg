package com.fantonio.entregarg.data.repository

import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.data.model.PrintJobPrice
import com.fantonio.entregarg.data.model.PrintJobRequest
import com.fantonio.entregarg.data.remote.RetrofitInstance

class JobRepository {
    private val api = RetrofitInstance.api

    suspend fun listJobs(): List<PrintJob> = api.getJobs()

    suspend fun getJob(id: Int): PrintJob = api.getJob(id)

    suspend fun createJob(job: PrintJobRequest): PrintJob = api.createJob(job)

    suspend fun updateJob(id: Int, job: PrintJobRequest): PrintJob = api.updateJob(id, job)

    suspend fun deleteJob(id: Int) = api.deleteJob(id)

    suspend fun getPriceInUsd(id: Int): PrintJobPrice = api.getJobPriceUsd(id)
}
