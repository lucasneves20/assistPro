package br.com.assistpro

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Calendar

/**
 * Calendario mensal proprio, desenhado com Views e com o tema do app.
 * Nao usa DatePicker do Android.
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
    private val grade = GridLayout(context)

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
        montar()
        atualizar()
    }

    fun ano(): Int = ano

    fun mes(): Int = mes

    fun selecionar(dia: Int?) {
        diaSelecionado = dia
        atualizar()
    }

    private fun montar() {
        val cabecalho = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        cabecalho.addView(botaoMes("<"), LinearLayout.LayoutParams(dp(40), dp(40)))
        titulo.apply {
            gravity = Gravity.CENTER
            setTextColor(cor(R.color.dracula_purple))
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
        }
        cabecalho.addView(
            titulo,
            LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        )
        cabecalho.addView(botaoMes(">"), LinearLayout.LayoutParams(dp(40), dp(40)))
        addView(cabecalho)

        val semana = LinearLayout(context).apply { orientation = HORIZONTAL }
        for (nome in diasSemana) {
            semana.addView(diaSemana(nome), LinearLayout.LayoutParams(0, dp(28), 1f))
        }
        addView(semana)

        grade.columnCount = 7
        grade.rowCount = 6
        addView(grade, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun atualizar() {
        titulo.text = Formato.mesTitulo(String.format("%04d-%02d", ano, mes))
        grade.removeAllViews()
        for (linha in CalendarioMes.celulas(ano, mes)) {
            for (dia in linha) {
                grade.addView(celula(dia), parametrosCelula())
            }
        }
    }

    private fun celula(dia: Int): TextView {
        val tv = TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 15f
            text = if (dia == 0) "" else dia.toString()
        }
        if (dia == 0) {
            return tv
        }
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
            else -> {
                tv.setTextColor(cor(R.color.dracula_fg))
            }
        }
        tv.setOnClickListener { aoSelecionarDia?.invoke(dia) }
        return tv
    }

    private fun parametrosCelula(): GridLayout.LayoutParams =
        GridLayout.LayoutParams().apply {
            width = 0
            height = dp(40)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setGravity(Gravity.CENTER)
            setMargins(dp(1), dp(1), dp(1), dp(1))
        }

    private fun diaSemana(nome: String): TextView = TextView(context).apply {
        text = nome
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(cor(R.color.dracula_comment))
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

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()
}
