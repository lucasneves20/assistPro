package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogAppTest {

    @Before
    fun limpar() {
        LogApp.limpar()
    }

    @Test
    fun registraELimpa() {
        LogApp.i("inicio")
        LogApp.w("aviso")
        assertTrue(LogApp.texto().contains("inicio"))
        assertTrue(LogApp.texto().contains("aviso"))
        LogApp.limpar()
        assertEquals("", LogApp.texto())
        assertNull(LogApp.ultimoErro())
    }

    @Test
    fun erroGuardaUltimoErro() {
        LogApp.e("falhou", IllegalStateException("boom"))
        val erro = LogApp.ultimoErro()
        assertNotNull(erro)
        assertTrue(erro!!.contains("falhou"))
        assertTrue(erro.contains("IllegalStateException"))
        assertTrue(LogApp.texto().contains("boom"))
    }

    @Test
    fun mantemLimiteDeLinhas() {
        repeat(400) { LogApp.i("linha $it") }
        val linhas = LogApp.texto().split("\n")
        assertTrue(linhas.size <= 300)
        assertTrue(LogApp.texto().contains("linha 399"))
    }
}
