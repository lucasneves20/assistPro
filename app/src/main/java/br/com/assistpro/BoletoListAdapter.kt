package br.com.assistpro

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BoletoListAdapter(
    private val onToggle: (Boleto) -> Unit,
    private val onDelete: (Boleto) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val itens = ArrayList<ListItem>()

    fun submit(novos: List<ListItem>) {
        itens.clear()
        itens.addAll(novos)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itens.size

    override fun getItemViewType(position: Int): Int =
        if (itens[position] is ListItem.Header) TIPO_HEADER else TIPO_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_HEADER) {
            HeaderHolder(inflater.inflate(R.layout.item_month_header, parent, false))
        } else {
            ItemHolder(inflater.inflate(R.layout.item_boleto, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val it = itens[position]) {
            is ListItem.Header -> (holder as HeaderHolder).bind(it)
            is ListItem.Item -> (holder as ItemHolder).bind(it.boleto)
        }
    }

    inner class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titulo: TextView = view.findViewById(R.id.header_titulo)
        private val resumo: TextView = view.findViewById(R.id.header_resumo)

        fun bind(h: ListItem.Header) {
            titulo.text = h.titulo
            val qtd = if (h.quantidade == 1) "1 boleto" else "${h.quantidade} boletos"
            resumo.text = "$qtd  •  ${Formato.moeda(h.total)}"
        }
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val descricao: TextView = view.findViewById(R.id.item_descricao)
        private val vencimento: TextView = view.findViewById(R.id.item_vencimento)
        private val valor: TextView = view.findViewById(R.id.item_valor)
        private val pagoBtn: ImageButton = view.findViewById(R.id.item_pago)
        private val excluirBtn: ImageButton = view.findViewById(R.id.item_excluir)

        fun bind(b: Boleto) {
            descricao.text = if (b.descricao.isNotBlank()) b.descricao else "Boleto"
            vencimento.text = if (b.vencimento != null) {
                "Vence em ${Formato.dataBrDeIso(b.vencimento)}"
            } else {
                "Sem vencimento"
            }
            valor.text = Formato.moeda(b.valorCentavos)

            if (b.pago) {
                valor.paintFlags = valor.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                descricao.alpha = 0.55f
                vencimento.alpha = 0.55f
                pagoBtn.setImageResource(R.drawable.ic_check_on)
            } else {
                valor.paintFlags = valor.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                descricao.alpha = 1f
                vencimento.alpha = 1f
                pagoBtn.setImageResource(R.drawable.ic_check_off)
            }

            pagoBtn.setOnClickListener { onToggle(b) }
            excluirBtn.setOnClickListener { onDelete(b) }
        }
    }

    companion object {
        private const val TIPO_HEADER = 0
        private const val TIPO_ITEM = 1
    }
}
