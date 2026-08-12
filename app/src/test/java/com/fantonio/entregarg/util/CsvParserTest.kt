package com.fantonio.entregarg.util

import org.junit.Test
import java.io.StringReader
import kotlin.test.assertEquals

class CsvParserTest {
    @Test
    fun `parseIdentities with header should return list of identities`() {
        val csvData = """
            Nome, CPF, Lote
            João Silva, 12345678901, Lote A
            Maria Souza, 98765432100, Lote B
        """.trimIndent()
        
        val reader = StringReader(csvData)
        val result = CsvParser.parseIdentities(reader)
        
        assertEquals(2, result.size)
        assertEquals("João Silva", result[0].nome)
        assertEquals("12345678901", result[0].cpf)
        assertEquals("Lote A", result[0].lote)
        assertEquals("Maria Souza", result[1].nome)
        assertEquals("Lote B", result[1].lote)
    }

    @Test
    fun `parseIdentities without header should return list of identities`() {
        val csvData = """
            Carlos Oliveira, 11122233344, Lote C
        """.trimIndent()
        
        val reader = StringReader(csvData)
        val result = CsvParser.parseIdentities(reader)
        
        assertEquals(1, result.size)
        assertEquals("Carlos Oliveira", result[0].nome)
    }
}
