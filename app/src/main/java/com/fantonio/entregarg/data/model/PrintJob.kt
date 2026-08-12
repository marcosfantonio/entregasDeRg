package com.fantonio.entregarg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Representa um job de impressão 3D da Entrega de RG.
 * Espelha o schema PrintJobOut do middleware FastAPI.
 */
data class PrintJob(
    val id: Int = 0,
    @SerializedName("client_name") val clientName: String,
    @SerializedName("item_name") val itemName: String,
    val material: String,
    @SerializedName("weight_grams") val weightGrams: Double,
    @SerializedName("print_hours") val printHours: Double,
    @SerializedName("cost_brl") val costBrl: Double,
    val status: String = "queued",
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("created_at") val createdAt: Long = 0
)

/** Corpo enviado em POST/PUT — não inclui id/created_at, que o servidor controla. */
data class PrintJobRequest(
    @SerializedName("client_name") val clientName: String,
    @SerializedName("item_name") val itemName: String,
    val material: String,
    @SerializedName("weight_grams") val weightGrams: Double,
    @SerializedName("print_hours") val printHours: Double,
    @SerializedName("cost_brl") val costBrl: Double,
    val status: String = "queued",
    @SerializedName("image_url") val imageUrl: String? = null
)

/** Resposta do endpoint /jobs/{id}/price, que consome a API pública de câmbio. */
data class PrintJobPrice(
    val id: Int,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("cost_brl") val costBrl: Double,
    @SerializedName("cost_usd") val costUsd: Double,
    @SerializedName("exchange_rate_brl_to_usd") val exchangeRate: Double
)
