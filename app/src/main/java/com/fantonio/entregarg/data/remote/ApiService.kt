package com.fantonio.entregarg.data.remote

import com.fantonio.entregarg.data.model.PrintJob
import com.fantonio.entregarg.data.model.PrintJobPrice
import com.fantonio.entregarg.data.model.PrintJobRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Contrato HTTP com o middleware FastAPI.
 * Cobre os 4 verbos exigidos: GET, POST, PUT e DELETE.
 */
interface ApiService {

    @GET("jobs")
    suspend fun getJobs(): List<PrintJob>

    @GET("jobs/{id}")
    suspend fun getJob(@Path("id") id: Int): PrintJob

    @POST("jobs")
    suspend fun createJob(@Body job: PrintJobRequest): PrintJob

    @PUT("jobs/{id}")
    suspend fun updateJob(@Path("id") id: Int, @Body job: PrintJobRequest): PrintJob

    @DELETE("jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Int): Response<Unit>

    // Endpoint que faz o middleware consumir a API pública de câmbio
    // e devolver o custo do job convertido para USD.
    @GET("jobs/{id}/price")
    suspend fun getJobPriceUsd(@Path("id") id: Int): PrintJobPrice
}
