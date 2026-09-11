package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Test

class RelatorioTest {

    private val hoje = "2025-06-15"

    private val boletos = listOf(
        Boleto(id = 1, vencimento = "2025-06-10", valorCentavos = 1000, pago = true),
        Boleto(id = 2, vencimento = "2025-06-20", valorCentavos = 2000, pago = false),
        Boleto(id = 3, vencimento = "2025-06-05", valorCentavos = 3000, pago = false),
        Boleto(id = 4, vencimento = "2025-05-30", valorCentavos = 5000, pago = false),
        Boleto(id = 5, vencimento = null, valorCentavos = 700, pago = false)
    )

    @Test
    fun resumoDoMesConsideraSoOMes() {
        val r = Relatorio.resumo(boletos, 2025, 6, hoje)
        assertEquals(6000L, r.total)
        assertEquals(1000L, r.pago)
        assertEquals(5000L, r.emAberto)
        assertEquals(3000L, r.vencido)
        assertEquals(3, r.quantidade)
    }

    @Test
    fun resumoDoDiaFiltraPorDia() {
        val r = Relatorio.resumoDoDia(boletos, 2025, 6, 5, hoje)
        assertEquals(3000L, r.total)
        assertEquals(0L, r.pago)
        assertEquals(3000L, r.emAberto)
        assertEquals(3000L, r.vencido)
        assertEquals(1, r.quantidade)
    }

    @Test
    fun mesSemBoletosZera() {
        val r = Relatorio.resumo(boletos, 2024, 1, hoje)
        assertEquals(0L, r.total)
        assertEquals(0, r.quantidade)
    }

    @Test
    fun pagoNaoContaComoVencido() {
        val r = Relatorio.resumo(
            listOf(Boleto(vencimento = "2025-06-01", valorCentavos = 500, pago = true)),
            2025, 6, hoje
        )
        assertEquals(500L, r.pago)
        assertEquals(0L, r.vencido)
        assertEquals(0L, r.emAberto)
    }

    @Test
    fun financeiroCalculaSaldoDoMes() {
        val ganhos = listOf(
            Ganho(data = "2025-06-01", valorCentavos = 10000),
            Ganho(data = "2025-06-20", valorCentavos = 5000),
            Ganho(data = "2025-05-01", valorCentavos = 999)
        )
        val f = Relatorio.financeiro(ganhos, boletos, 2025, 6)
        assertEquals(15000L, f.ganhos)
        assertEquals(6000L, f.perdas)
        assertEquals(9000L, f.saldo)
        assertEquals(2, f.qtdGanhos)
    }

    @Test
    fun financeiroDoDiaFiltra() {
        val ganhos = listOf(Ganho(data = "2025-06-05", valorCentavos = 8000))
        val f = Relatorio.financeiroDoDia(ganhos, boletos, 2025, 6, 5)
        assertEquals(8000L, f.ganhos)
        assertEquals(3000L, f.perdas)
        assertEquals(5000L, f.saldo)
    }

    @Test
    fun porDiaTemTodosOsDiasComTotais() {
        val ganhos = listOf(Ganho(data = "2025-06-05", valorCentavos = 8000))
        val serie = Relatorio.porDia(ganhos, boletos, 2025, 6)
        assertEquals(30, serie.size)

        val dia5 = serie.first { it.dia == 5 }
        assertEquals(8000L, dia5.ganhos)
        assertEquals(3000L, dia5.perdas)

        val dia10 = serie.first { it.dia == 10 }
        assertEquals(1000L, dia10.perdas)
        assertEquals(0L, dia10.ganhos)
    }
}
