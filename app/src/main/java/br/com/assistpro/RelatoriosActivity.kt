package br.com.assistpro

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class RelatoriosActivity : AppCompatActivity() {

    private val repo by lazy { BoletoRepository(this) }

    private lateinit var calendario: CalendarioMesView
    private lateinit var escopo: TextView
    private lateinit var linhaTotal: TextView
    private lateinit var linhaPago: TextView
    private lateinit var linhaAberto: TextView
    private lateinit var linhaVencido: TextView
    private lateinit var lista: LinearLayout

    private var boletos: List<Boleto> = emptyList()
    private var anoAtual = 0
    private var mesAtual = 0
    private var diaAtual: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_relatorios)

        calendario = findViewById(R.id.calendario)
        escopo = findViewById(R.id.escopo)
        linhaTotal = findViewById(R.id.linha_total)
        linhaPago = findViewById(R.id.linha_pago)
        linhaAberto = findViewById(R.id.linha_aberto)
        linhaVencido = findViewById(R.id.linha_vencido)
        lista = findViewById(R.id.lista_relatorio)

        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Relatórios")

        anoAtual = calendario.ano()
        mesAtual = calendario.mes()

        calendario.aoTrocarMes = { a, m ->
            anoAtual = a
            mesAtual = m
            diaAtual = null
            render()
        }
        calendario.aoSelecionarDia = { dia ->
            diaAtual = if (diaAtual == dia) null else dia
            calendario.selecionar(diaAtual)
            render()
        }

        carregar()
    }

    override fun onResume() {
        super.onResume()
        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Relatórios")
        carregar()
    }

    private fun carregar() {
        Thread {
            val b = repo.listar()
            runOnUiThread {
                boletos = b
                render()
            }
        }.start()
    }

    private fun noEscopo(): List<Boleto> {
        val dia = diaAtual
        return if (dia != null) {
            val iso = String.format("%04d-%02d-%02d", anoAtual, mesAtual, dia)
            boletos.filter { it.vencimento == iso }
        } else {
            val prefixo = String.format("%04d-%02d", anoAtual, mesAtual)
            boletos.filter { it.vencimento?.startsWith(prefixo) == true }
        }
    }

    private fun render() {
        val hoje = CalendarioMes.hojeIso()
        val dia = diaAtual
        val r = if (dia != null) {
            Relatorio.resumoDoDia(boletos, anoAtual, mesAtual, dia, hoje)
        } else {
            Relatorio.resumo(boletos, anoAtual, mesAtual, hoje)
        }

        escopo.text = if (dia != null) {
            String.format("Dia %02d/%02d/%04d", dia, mesAtual, anoAtual)
        } else {
            Formato.mesTitulo(String.format("%04d-%02d", anoAtual, mesAtual))
        }

        linhaTotal.text = "Total: ${Formato.moeda(r.total)}  •  ${r.quantidade} boleto(s)"
        linhaPago.text = "Pago: ${Formato.moeda(r.pago)}"
        linhaAberto.text = "Em aberto: ${Formato.moeda(r.emAberto)}"
        linhaVencido.text = "Vencido: ${Formato.moeda(r.vencido)}"

        lista.removeAllViews()
        val itens = noEscopo().sortedBy { it.vencimento }
        if (itens.isEmpty()) {
            lista.addView(rotulo(getString(R.string.relatorio_vazio), R.color.dracula_comment))
        } else {
            for (b in itens) lista.addView(linhaBoleto(b))
        }
    }

    private fun linhaBoleto(b: Boleto): View {
        val linha = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val nome = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            val titulo = if (b.descricao.isNotBlank()) b.descricao else "Boleto"
            val marca = if (b.pago) "[Pago] " else ""
            text = "$marca$titulo  •  ${Formato.dataBrDeIso(b.vencimento)}"
            setTextColor(cor(if (b.pago) R.color.dracula_comment else R.color.dracula_fg))
            textSize = 14f
        }
        val valor = TextView(this).apply {
            text = Formato.moeda(b.valorCentavos)
            setTextColor(cor(R.color.dracula_green))
            textSize = 14f
        }
        linha.addView(nome)
        linha.addView(valor)
        return linha
    }

    private fun rotulo(texto: String, corRes: Int): TextView = TextView(this).apply {
        text = texto
        setTextColor(cor(corRes))
        textSize = 14f
        setPadding(0, dp(16), 0, dp(16))
    }

    private fun cor(id: Int): Int = ContextCompat.getColor(this, id)

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
