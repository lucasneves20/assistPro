package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoletoTest {

    @Test
    fun valoresPadrao() {
        val b = Boleto()
        assertEquals(0L, b.id)
        assertEquals("", b.linha)
        assertEquals(0L, b.valorCentavos)
        assertNull(b.vencimento)
        assertEquals("", b.descricao)
        assertNull(b.imagem)
        assertFalse(b.pago)
    }

    @Test
    fun copyPreservaIdPagoECriacao() {
        val b = Boleto(id = 7, pago = true, valorCentavos = 100, criadoEm = 123)
        val c = b.copy(valorCentavos = 200, descricao = "Internet")
        assertEquals(7L, c.id)
        assertTrue(c.pago)
        assertEquals(123L, c.criadoEm)
        assertEquals(200L, c.valorCentavos)
        assertEquals("Internet", c.descricao)
    }
}
