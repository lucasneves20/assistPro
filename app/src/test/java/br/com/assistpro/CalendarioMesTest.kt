package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarioMesTest {

    @Test
    fun janeiro2024ComecaNaSegunda() {
        val semanas = CalendarioMes.celulas(2024, 1)
        assertTrue(semanas.all { it.size == 7 })
        assertEquals(0, semanas[0][0])
        assertEquals(1, semanas[0][1])
        val dias = semanas.flatten().filter { it != 0 }
        assertEquals(31, dias.size)
        assertEquals((1..31).toList(), dias)
    }

    @Test
    fun fevereiroBissexto2024Tem29Dias() {
        val dias = CalendarioMes.celulas(2024, 2).flatten().filter { it != 0 }
        assertEquals(29, dias.size)
        assertEquals(29, dias.max())
    }

    @Test
    fun totalDeCelulasEhMultiploDeSete() {
        for (mes in 1..12) {
            val total = CalendarioMes.celulas(2025, mes).sumOf { it.size }
            assertEquals(0, total % 7)
        }
    }
}
