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

    data class DiaValor(val dia: Int, val ganhos: Long, val perdas: Long)

    /** Serie diaria do mes (1..ultimo dia) com ganhos e perdas por dia. */
    fun porDia(ganhos: List<Ganho>, boletos: List<Boleto>, ano: Int, mes: Int): List<DiaValor> {
        val dias = CalendarioMes.celulas(ano, mes).flatten().maxOrNull() ?: 0
        if (dias <= 0) return emptyList()
        val prefixo = String.format("%04d-%02d", ano, mes)
        val porGanho = LongArray(dias + 1)
        val porPerda = LongArray(dias + 1)
        for (g in ganhos) {
            if (g.data.length >= 10 && g.data.startsWith(prefixo)) {
                val d = g.data.substring(8, 10).toIntOrNull() ?: 0
                if (d in 1..dias) porGanho[d] += g.valorCentavos
            }
        }
        for (b in boletos) {
            val v = b.vencimento ?: continue
            if (v.length >= 10 && v.startsWith(prefixo)) {
                val d = v.substring(8, 10).toIntOrNull() ?: 0
                if (d in 1..dias) porPerda[d] += b.valorCentavos
            }
        }
        return (1..dias).map { DiaValor(it, porGanho[it], porPerda[it]) }
    }
}
