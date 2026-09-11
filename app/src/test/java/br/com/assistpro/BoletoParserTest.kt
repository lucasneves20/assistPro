package br.com.assistpro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BoletoParserTest {

    private fun linhaBancaria(fator: Int, valorCentavos: Long): String {
        val prefixo = StringBuilder().apply {
            append("3419")
            repeat(29) { append('0') }
        }.toString()
        val campo5 = String.format("%04d%010d", fator, valorCentavos)
        return prefixo + campo5
    }

    private fun formatar(linha: String): String =
        "${linha.substring(0, 5)}.${linha.substring(5, 10)} " +
            "${linha.substring(10, 15)}.${linha.substring(15, 21)} " +
            "${linha.substring(21, 26)}.${linha.substring(26, 32)} " +
            "${linha.substring(32, 33)} ${linha.substring(33, 47)}"

    @Test
    fun onlyDigitsRemoveSeparadores() {
        assertEquals("34190000", BoletoParser.onlyDigits("34.190 000"))
        assertEquals("", BoletoParser.onlyDigits("abc"))
    }

    @Test
    fun parseLinhaBancariaFatorBase() {
        val linha = linhaBancaria(1000, 12345)
        assertEquals(47, linha.length)
        val (valor, venc) = BoletoParser.parseLinha(linha)
        assertEquals(12345L, valor)
        assertEquals("2025-02-22", venc)
    }

    @Test
    fun parseLinhaBancariaFatorAcimaDaBase() {
        val (valor, venc) = BoletoParser.parseLinha(linhaBancaria(1001, 99999))
        assertEquals(99999L, valor)
        assertEquals("2025-02-23", venc)
    }

    @Test
    fun parseLinhaBancariaFatorZeroSemVencimento() {
        val (valor, venc) = BoletoParser.parseLinha(linhaBancaria(0, 500))
        assertEquals(500L, valor)
        assertNull(venc)
    }

    @Test
    fun parseLinhaTamanhoInvalido() {
        val (valor, venc) = BoletoParser.parseLinha("123")
        assertNull(valor)
        assertNull(venc)
    }

    @Test
    fun parseLinhaArrecadacao() {
        val b = StringBuilder()
        b.append('8')
        b.append("123")
        b.append("00000001234")
        b.append("5678")
        b.append("20250615")
        repeat(21) { b.append('0') }
        val linha = b.toString()
        assertEquals(48, linha.length)
        val (valor, venc) = BoletoParser.parseLinha(linha)
        assertEquals(1234L, valor)
        assertEquals("2025-06-15", venc)
    }

    @Test
    fun findLinhaEmTextoFormatado() {
        val linha = linhaBancaria(1000, 12345)
        val texto = "Pague no banco:\n${formatar(linha)}\n"
        assertEquals(linha, BoletoParser.findLinha(texto))
    }

    @Test
    fun findValorUsaFormatoBrasileiro() {
        assertEquals(123456L, BoletoParser.findValor("Valor: R$ 1.234,56 no vencimento"))
        assertEquals(1234L, BoletoParser.findValor("total 12,34"))
        assertNull(BoletoParser.findValor("sem valor"))
    }

    @Test
    fun findVencimentoPrefereTrechoComVenc() {
        assertEquals("2025-06-15", BoletoParser.findVencimento("Vencimento: 15/06/2025"))
        assertEquals("2025-01-05", BoletoParser.findVencimento("vence em 05/01/2025"))
        assertNull(BoletoParser.findVencimento("sem data aqui"))
    }

    @Test
    fun parseComLinhaUsaDadosDaLinha() {
        val linha = linhaBancaria(1000, 99999)
        val texto = "Linha digitavel: ${formatar(linha)}\nValor: R$ 12,34\nVencimento: 10/07/2025"
        val r = BoletoParser.parse(texto)
        assertEquals(linha, r.linha)
        assertEquals(99999L, r.valorCentavos)
        assertEquals("2025-02-22", r.vencimentoIso)
    }

    @Test
    fun parseSemLinhaUsaTexto() {
        val r = BoletoParser.parse("Valor: R$ 1.234,56  Vencimento: 15/06/2025")
        assertNull(r.linha)
        assertEquals(123456L, r.valorCentavos)
        assertEquals("2025-06-15", r.vencimentoIso)
    }

    @Test
    fun parseTextoSemDados() {
        val r = BoletoParser.parse("nenhum dado de boleto aqui")
        assertNull(r.linha)
        assertNull(r.valorCentavos)
        assertNull(r.vencimentoIso)
    }
}
