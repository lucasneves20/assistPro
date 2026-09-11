package br.com.assistpro

/**
 * Agregacoes financeiras usadas pelo dashboard (RelatoriosActivity).
 */
object Relatorio {

    data class Resumo(
        val total: Long,
        val pago: Long,
        val emAberto: Long,
        val vencido: Long,
        val quantidade: Int
    )

    fun resumo(boletos: List<Boleto>, ano: Int, mes: Int, hojeIso: String): Resumo {
        val prefixo = String.format("%04d-%02d", ano, mes)
        return resumoDe(boletos.filter { it.vencimento?.startsWith(prefixo) == true }, hojeIso)
    }

    fun resumoDoDia(
        boletos: List<Boleto>,
        ano: Int,
        mes: Int,
        dia: Int,
        hojeIso: String
    ): Resumo {
        val iso = String.format("%04d-%02d-%02d", ano, mes, dia)
        return resumoDe(boletos.filter { it.vencimento == iso }, hojeIso)
    }

    private fun resumoDe(lista: List<Boleto>, hojeIso: String): Resumo {
        val total = lista.sumOf { it.valorCentavos }
        val pago = lista.filter { it.pago }.sumOf { it.valorCentavos }
        val emAberto = lista.filterNot { it.pago }.sumOf { it.valorCentavos }
        val vencido = lista.filterNot { it.pago }
            .filter { (it.vencimento ?: "") < hojeIso }
            .sumOf { it.valorCentavos }
        return Resumo(total, pago, emAberto, vencido, lista.size)
    }

    data class Financeiro(
        val ganhos: Long,
        val perdas: Long,
        val saldo: Long,
        val qtdGanhos: Int
    )

    fun financeiro(ganhos: List<Ganho>, boletos: List<Boleto>, ano: Int, mes: Int): Financeiro {
        val prefixo = String.format("%04d-%02d", ano, mes)
        return financeiroDe(
            ganhos.filter { it.data.startsWith(prefixo) },
            boletos.filter { it.vencimento?.startsWith(prefixo) == true }
        )
    }

    fun financeiroDoDia(
        ganhos: List<Ganho>,
        boletos: List<Boleto>,
        ano: Int,
        mes: Int,
        dia: Int
    ): Financeiro {
        val iso = String.format("%04d-%02d-%02d", ano, mes, dia)
        return financeiroDe(
            ganhos.filter { it.data == iso },
            boletos.filter { it.vencimento == iso }
        )
    }

    private fun financeiroDe(g: List<Ganho>, b: List<Boleto>): Financeiro {
        val ganhos = g.sumOf { it.valorCentavos }
        val perdas = b.sumOf { it.valorCentavos }
        return Financeiro(ganhos, perdas, ganhos - perdas, g.size)
    }
}
