package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Test

class ModoDevTest {

    @Test
    fun tipoMapeiaBugEFeedback() {
        assertEquals("bug", ModoDev.tipo(true))
        assertEquals("feedback", ModoDev.tipo(false))
    }

    @Test
    fun tipoNaoEhVazio() {
        assertEquals(ModoDev.TIPO_BUG, ModoDev.tipo(true))
        assertEquals(ModoDev.TIPO_FEEDBACK, ModoDev.tipo(false))
    }
}
