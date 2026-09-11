package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListItemTest {

    @Test
    fun agrupaPorMesEOrdena() {
        val a = Boleto(id = 1, vencimento = "2025-06-15", valorCentavos = 1000)
        val b = Boleto(id = 2, vencimento = "2025-06-01", valorCentavos = 2000)
        val c = Boleto(id = 3, vencimento = null, valorCentavos = 500)
        val d = Boleto(id = 4, vencimento = "2025-05-10", valorCentavos = 300)

        val itens = ListItem.agrupar(listOf(a, b, c, d))
        val headers = itens.filterIsInstance<ListItem.Header>()

        assertEquals(listOf("2025-05", "2025-06", "sem"), headers.map { it.chave })
        assertEquals(4, itens.count { it is ListItem.Item })
    }

    @Test
    fun headersTemTotalEQuantidade() {
        val a = Boleto(id = 1, vencimento = "2025-06-15", valorCentavos = 1000)
        val b = Boleto(id = 2, vencimento = "2025-06-01", valorCentavos = 2000)
        val c = Boleto(id = 3, vencimento = null, valorCentavos = 500)

        val headers = ListItem.agrupar(listOf(a, b, c)).filterIsInstance<ListItem.Header>()
        val junho = headers.first { it.chave == "2025-06" }
        val sem = headers.first { it.chave == "sem" }

        assertEquals(3000L, junho.total)
        assertEquals(2, junho.quantidade)
        assertEquals(500L, sem.total)
        assertEquals("Sem vencimento", sem.titulo)
    }

    @Test
    fun agruparListaVazia() {
        assertTrue(ListItem.agrupar(emptyList()).isEmpty())
    }
}
