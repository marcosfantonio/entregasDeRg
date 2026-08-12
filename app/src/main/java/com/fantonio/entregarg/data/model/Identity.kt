package com.fantonio.entregarg.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "identities")
data class Identity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val cpf: String,
    val lote: String,
    val retirada: Boolean = false,
    val retiradaPor: String? = null,
    val retiradaData: Long? = null // Timestamp
)
