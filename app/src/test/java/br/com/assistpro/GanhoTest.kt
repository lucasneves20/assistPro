package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GanhoTest {

    @Test
    fun valoresPadrao() {
        val g = Ganho()
        assertEquals(0L, g.id)
        assertEquals("", g.descricao)
        assertEquals(0L, g.valorCentavos)
        assertEquals("", g.data)
        assertTrue(g.criadoEm > 0L)
    }
}
