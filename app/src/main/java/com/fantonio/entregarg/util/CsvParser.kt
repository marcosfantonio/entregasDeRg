package com.fantonio.entregarg.util

import com.fantonio.entregarg.data.model.Identity
import com.opencsv.CSVReader
import java.io.Reader

object CsvParser {
    fun parseIdentities(reader: Reader): List<Identity> {
        val csvReader = CSVReader(reader)
        val rows = csvReader.readAll()
        
        val startIdx = if (rows.firstOrNull()?.get(0)?.contains("nome", ignoreCase = true) == true) 1 else 0
        
        return rows.drop(startIdx).mapNotNull { columns ->
            if (columns.size >= 3) {
                Identity(
                    nome = columns[0].trim(),
                    cpf = columns[1].trim(),
                    lote = columns[2].trim()
                )
            } else null
        }
    }
}
