package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DadosExemploTest {

    @Test
    fun boletosDoMesTemDatasValidas() {
        val lista = DadosExemplo.boletos(2025, 6)
        assertEquals(7, lista.size)
        assertTrue(lista.all { it.vencimento != null && it.vencimento!!.startsWith("2025-06") })
        assertTrue("deveria ter boletos pagos", lista.any { it.pago })
        assertTrue("deveria ter boletos em aberto", lista.any { !it.pago })
    }

    @Test
    fun ganhosDoMesTemDatasValidas() {
        val lista = DadosExemplo.ganhos(2025, 6)
        assertEquals(4, lista.size)
        assertTrue(lista.all { it.data.startsWith("2025-06") })
        assertTrue(lista.all { it.valorCentavos > 0 })
    }

    @Test
    fun dashboardFicaComNumeros() {
        val boletos = DadosExemplo.boletos(2025, 6)
        val ganhos = DadosExemplo.ganhos(2025, 6)

        val financeiro = Relatorio.financeiro(ganhos, boletos, 2025, 6)
        assertTrue(financeiro.ganhos > 0)
        assertTrue(financeiro.perdas > 0)
        assertTrue(financeiro.saldo > 0)

        val serie = Relatorio.porDia(ganhos, boletos, 2025, 6)
        assertEquals(30, serie.size)
        assertTrue("grafico precisa de barras de ganho", serie.any { it.ganhos > 0 })
        assertTrue("grafico precisa de barras de perda", serie.any { it.perdas > 0 })
    }
}
