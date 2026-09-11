package br.com.assistpro

import android.os.Handler
import android.os.Looper
import android.view.View

/**
 * Mostra um overlay de carregamento apenas se o preparo da tela demorar mais
 * que [atrasoPadrao] (evita piscar em telas rapidas). Com atraso <= 0 aparece na hora.
 */
class Carregador(
    private val overlay: View,
    private val atrasoPadrao: Long = 1000L
) {

    private val handler = Handler(Looper.getMainLooper())
    private var pendente: Runnable? = null

    fun iniciar(atrasoMs: Long = atrasoPadrao) {
        cancelar()
        if (atrasoMs <= 0L) {
            overlay.visibility = View.VISIBLE
            return
        }
        val tarefa = Runnable { overlay.visibility = View.VISIBLE }
        pendente = tarefa
        handler.postDelayed(tarefa, atrasoMs)
    }

    fun finalizar() {
        cancelar()
        overlay.visibility = View.GONE
    }

    fun cancelar() {
        pendente?.let { handler.removeCallbacks(it) }
        pendente = null
    }
}
