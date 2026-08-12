package com.fantonio.entregarg.util

import com.fantonio.entregarg.data.model.Identity
import com.opencsv.CSVReader
import java.io.Reader

object CsvParser {
    fun parseIdentities(reader: Reader): List<Identity> {
        val csvReader = CSVReader(reader)
        val rows = csvReader.readAll()
        
        if (rows.isEmpty()) return emptyList()

        val header = rows.first().map { it.trim().lowercase() }
        
        val nomeIdx = header.indexOfFirst { it.contains("nome") || it.contains("pessoa") }
        val cpfIdx = header.indexOfFirst { it.contains("cpf") || it.contains("rg") || it.contains("doc") }
        val loteIdx = header.indexOfFirst { it.contains("lote") || it.contains("lt") }

        // Se não encontrar os índices, assume a ordem padrão 0, 1, 2 e pula o header se parece ser um
        val actualNomeIdx = if (nomeIdx != -1) nomeIdx else 0
        val actualCpfIdx = if (cpfIdx != -1) cpfIdx else 1
        val actualLoteIdx = if (loteIdx != -1) loteIdx else 2

        val startIdx = if (nomeIdx != -1 || cpfIdx != -1 || loteIdx != -1) 1 else 0
        
        return rows.drop(startIdx).mapNotNull { columns ->
            if (columns.size > maxOf(actualNomeIdx, actualCpfIdx, actualLoteIdx)) {
                val nome = columns[actualNomeIdx].trim()
                val cpf = columns[actualCpfIdx].trim()
                val lote = columns[actualLoteIdx].trim()
                
                if (nome.isNotEmpty() && cpf.isNotEmpty()) {
                    Identity(
                        nome = nome,
                        cpf = cpf,
                        lote = lote.ifEmpty { "S/L" }
                    )
                } else null
            } else null
        }
    }
}
