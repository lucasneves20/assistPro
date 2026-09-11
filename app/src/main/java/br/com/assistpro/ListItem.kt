package br.com.assistpro

sealed class ListItem {
    data class Header(
        val chave: String,
        val titulo: String,
        val total: Long,
        val quantidade: Int
    ) : ListItem()

    data class Item(val boleto: Boleto) : ListItem()

    companion object {
        fun agrupar(boletos: List<Boleto>): List<ListItem> {
            val grupos = LinkedHashMap<String, MutableList<Boleto>>()
            for (b in boletos) {
                val chave = b.vencimento?.take(7) ?: "sem"
                grupos.getOrPut(chave) { ArrayList() }.add(b)
            }
            val chaves = grupos.keys.sortedWith(compareBy({ it == "sem" }, { it }))
            val out = ArrayList<ListItem>()
            for (k in chaves) {
                val lista = grupos[k] ?: continue
                val total = lista.sumOf { it.valorCentavos }
                val titulo = if (k == "sem") "Sem vencimento" else Formato.mesTitulo(k)
                out.add(Header(k, titulo, total, lista.size))
                for (b in lista) out.add(Item(b))
            }
            return out
        }
    }
}
