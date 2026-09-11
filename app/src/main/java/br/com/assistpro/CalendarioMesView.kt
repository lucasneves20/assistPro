package br.com.assistpro

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * Calendario mensal proprio, desenhado com Views e com o tema do app.
 * Nao usa DatePicker do Android. Cada linha (semana) e um LinearLayout com
 * 7 celulas de largura 0 + peso 1, o que funciona de forma confiavel tambem
 * dentro de um dialogo (modal).
 */
class CalendarioMesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var ano: Int
    private var mes: Int
    private var diaSelecionado: Int? = null

    var aoSelecionarDia: ((Int) -> Unit)? = null
    var aoTrocarMes: ((Int, Int) -> Unit)? = null

    private val titulo = TextView(context)
    private val corpo = LinearLayout(context)

    private val hojeAno: Int
    private val hojeMes: Int
    private val hojeDia: Int

    private val diasSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab")

    init {
        orientation = VERTICAL
        setBackgroundColor(cor(R.color.dracula_bg))
        val cal = Calendar.getInstance()
        hojeAno = cal.get(Calendar.YEAR)
        hojeMes = cal.get(Calendar.MONTH) + 1
        hojeDia = cal.get(Calendar.DAY_OF_MONTH)
        ano = hojeAno
        mes = hojeMes
        corpo.orientation = VERTICAL
        montar()
        atualizar()
    }

    fun ano(): Int = ano

    fun mes(): Int = mes

    fun selecionar(dia: Int?) {
        diaSelecionado = dia
        atualizar()
    }

    fun definirMes(ano: Int, mes: Int) {
        this.ano = ano
        this.mes = mes
        atualizar()
    }

    private fun montar() {
        val cabecalho = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        cabecalho.addView(botaoMes("<"), LinearLayout.LayoutParams(dp(44), dp(44)))
        titulo.apply {
            gravity = Gravity.CENTER
            setTextColor(cor(R.color.dracula_purple))
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
        }
        cabecalho.addView(titulo, LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        cabecalho.addView(botaoMes(">"), LinearLayout.LayoutParams(dp(44), dp(44)))
        addView(
            cabecalho,
            LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )

        val semana = LinearLayout(context).apply { orientation = HORIZONTAL }
        for (nome in diasSemana) {
            semana.addView(rotuloSemana(nome), celulaParams(dp(24)))
        }
        addView(
            semana,
            LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )

        addView(
            corpo,
            LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        )
    }

    private fun atualizar() {
        titulo.text = Formato.mesTitulo(String.format("%04d-%02d", ano, mes))
        corpo.removeAllViews()
        for (linha in CalendarioMes.celulas(ano, mes)) {
            val row = LinearLayout(context).apply { orientation = HORIZONTAL }
            for (dia in linha) row.addView(celula(dia), celulaParams(dp(40)))
            corpo.addView(
                row,
                LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
        }
    }

    private fun celulaParams(altura: Int): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(0, altura, 1f)

    private fun rotuloSemana(nome: String): TextView = TextView(context).apply {
        text = nome
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(cor(R.color.dracula_comment))
    }

    private fun celula(dia: Int): TextView {
        val tv = TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 15f
            text = if (dia == 0) "" else dia.toString()
        }
        if (dia == 0) return tv
        when {
            dia == diaSelecionado -> {
                tv.setTextColor(cor(R.color.dracula_bg))
                tv.setTypeface(tv.typeface, Typeface.BOLD)
                tv.background = circulo(cor(R.color.dracula_purple))
            }
            dia == hojeDia && mes == hojeMes && ano == hojeAno -> {
                tv.setTextColor(cor(R.color.dracula_purple))
                tv.setTypeface(tv.typeface, Typeface.BOLD)
            }
            else -> tv.setTextColor(cor(R.color.dracula_fg))
        }
        tv.setOnClickListener { aoSelecionarDia?.invoke(dia) }
        return tv
    }

    private fun botaoMes(simbolo: String): TextView = TextView(context).apply {
        text = simbolo
        gravity = Gravity.CENTER
        textSize = 22f
        setTextColor(cor(R.color.dracula_cyan))
        setOnClickListener { mudarMes(if (simbolo == "<") -1 else 1) }
    }

    private fun mudarMes(delta: Int) {
        mes += delta
        if (mes < 1) {
            mes = 12
            ano--
        } else if (mes > 12) {
            mes = 1
            ano++
        }
        diaSelecionado = null
        atualizar()
        aoTrocarMes?.invoke(ano, mes)
    }

    private fun circulo(cor: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(cor)
        }

    private fun cor(id: Int): Int = ContextCompat.getColor(context, id)

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
