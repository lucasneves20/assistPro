package br.com.assistpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Grafico de barras por dia do mes: verde = ganhos, vermelho = perdas.
 * Desenhado no Canvas, com o tema do app.
 */
class GraficoBarrasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var dados: List<Relatorio.DiaValor> = emptyList()

    private val pGanho = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dracula_green)
        style = Paint.Style.FILL
    }
    private val pPerda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dracula_red)
        style = Paint.Style.FILL
    }
    private val pBase = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dracula_current)
        strokeWidth = 2f
    }
    private val pTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.dracula_comment)
        textSize = resources.displayMetrics.density * 9f
    }

    fun definirDados(novos: List<Relatorio.DiaValor>) {
        dados = novos
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val esquerda = paddingLeft.toFloat()
        val topo = paddingTop.toFloat()
        val largura = width - paddingLeft - paddingRight
        val altura = height - paddingTop - paddingBottom
        if (largura <= 0f || altura <= 0f) return

        val baseY = topo + altura
        canvas.drawLine(esquerda, baseY, esquerda + largura, baseY, pBase)
        if (dados.isEmpty()) return

        val maximo = maxOf(dados.maxOf { it.ganhos }, dados.maxOf { it.perdas }, 1L).toFloat()
        val slot = largura / dados.size
        val barra = (slot * 0.30f).coerceAtLeast(1.5f)

        for ((i, d) in dados.withIndex()) {
            val x = esquerda + i * slot
            val hGanho = (d.ganhos / maximo) * altura
            val hPerda = (d.perdas / maximo) * altura
            if (d.ganhos > 0) {
                canvas.drawRect(
                    x + slot * 0.14f, baseY - hGanho,
                    x + slot * 0.14f + barra, baseY, pGanho
                )
            }
            if (d.perdas > 0) {
                canvas.drawRect(
                    x + slot * 0.52f, baseY - hPerda,
                    x + slot * 0.52f + barra, baseY, pPerda
                )
            }
            if (d.dia == 1 || d.dia % 5 == 0) {
                canvas.drawText(d.dia.toString(), x, baseY + pTexto.textSize + dp(2), pTexto)
            }
        }
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
