package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatoTest {

    @Test
    fun moedaFormataComSeparadores() {
        assertEquals("R$ 0,00", Formato.moeda(0))
        assertEquals("R$ 12,34", Formato.moeda(1234))
        assertEquals("R$ 1.234,56", Formato.moeda(123456))
        assertEquals("R$ 1.234.567,89", Formato.moeda(123456789))
        assertEquals("-R$ 5,00", Formato.moeda(-500))
    }

    @Test
    fun parseMoedaAceitaFormatosComuns() {
        assertEquals(123456L, Formato.parseMoeda("R$ 1.234,56"))
        assertEquals(123456L, Formato.parseMoeda("1234,56"))
        assertEquals(123456L, Formato.parseMoeda("1234.56"))
        assertEquals(500L, Formato.parseMoeda("5"))
        assertNull(Formato.parseMoeda(""))
        assertNull(Formato.parseMoeda("abc"))
    }

    @Test
    fun dataBrDeIsoConverte() {
        assertEquals("15/06/2025", Formato.dataBrDeIso("2025-06-15"))
        assertEquals("", Formato.dataBrDeIso(null))
        assertEquals("", Formato.dataBrDeIso(""))
    }

    @Test
    fun isoDeDataBrConverte() {
        assertEquals("2025-06-15", Formato.isoDeDataBr("15/06/2025"))
        assertEquals("2025-06-15", Formato.isoDeDataBr("15-06-2025"))
        assertEquals("2025-06-15", Formato.isoDeDataBr(" 15/06/2025 "))
        assertNull(Formato.isoDeDataBr("sem data"))
        assertNull(Formato.isoDeDataBr("15/13/2025"))
    }

    @Test
    fun mesTituloFormataAnoEMes() {
        val t = Formato.mesTitulo("2025-06")
        assertTrue(t.endsWith("2025"))
        assertTrue(t.contains(" de "))
        assertEquals("abc", Formato.mesTitulo("abc"))
    }
}
