package br.com.assistpro

import android.content.Context
import java.util.Calendar

/**
 * Gera dados ficticios (boletos e ganhos) no mes corrente, para visualizar
 * o dashboard com numeros. Usado pela tela de Configuracoes.
 */
object DadosExemplo {

    fun boletos(ano: Int, mes: Int): List<Boleto> = listOf(
        Boleto(valorCentavos = 12990, vencimento = iso(ano, mes, 5), descricao = "Internet", pago = true),
        Boleto(valorCentavos = 18750, vencimento = iso(ano, mes, 8), descricao = "Energia", pago = true),
        Boleto(valorCentavos = 8930, vencimento = iso(ano, mes, 10), descricao = "Agua"),
        Boleto(valorCentavos = 45000, vencimento = iso(ano, mes, 15), descricao = "Condominio"),
        Boleto(valorCentavos = 32000, vencimento = iso(ano, mes, 20), descricao = "Cartao de credito"),
        Boleto(valorCentavos = 7990, vencimento = iso(ano, mes, 25), descricao = "Telefone"),
        Boleto(valorCentavos = 5990, vencimento = iso(ano, mes, 28), descricao = "Streaming", pago = true)
    )

    fun ganhos(ano: Int, mes: Int): List<Ganho> = listOf(
        Ganho(descricao = "Salario", valorCentavos = 350000, data = iso(ano, mes, 5)),
        Ganho(descricao = "Freela", valorCentavos = 120000, data = iso(ano, mes, 15)),
        Ganho(descricao = "Venda", valorCentavos = 45000, data = iso(ano, mes, 20)),
        Ganho(descricao = "Reembolso", valorCentavos = 8000, data = iso(ano, mes, 25))
    )

    /** Insere os dados ficticios no banco. Retorna quantos registros foram criados. */
    fun gerar(context: Context): Int {
        val cal = Calendar.getInstance()
        val ano = cal.get(Calendar.YEAR)
        val mes = cal.get(Calendar.MONTH) + 1
        val repoBoletos = BoletoRepository(context)
        val repoGanhos = GanhoRepository(context)
        var total = 0
        for (b in boletos(ano, mes)) {
            repoBoletos.inserir(b)
            total++
        }
        for (g in ganhos(ano, mes)) {
            repoGanhos.inserir(g)
            total++
        }
        LogApp.i("Dados de exemplo gerados: $total registros")
        return total
    }

    private fun iso(ano: Int, mes: Int, dia: Int): String =
        String.format("%04d-%02d-%02d", ano, mes, dia)
}
