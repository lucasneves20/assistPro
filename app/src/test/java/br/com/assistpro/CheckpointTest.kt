package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckpointTest {

    @Test
    fun valoresPadrao() {
        val c = Checkpoint()
        assertEquals(0L, c.id)
        assertEquals("", c.tela)
        assertEquals("", c.versaoApp)
        assertFalse(c.temErro)
        assertNull(c.ultimoErro)
        assertEquals("", c.app)
        assertEquals("", c.sistema)
        assertEquals("", c.logs)
        assertTrue(c.criadoEm > 0L)
    }
}
