package br.com.assistpro

import java.util.Calendar
import java.util.Locale

/**
 * Calculos do calendario proprio (sem usar DatePicker do Android).
 */
object CalendarioMes {

    /** Semanas do mes; cada semana tem 7 posicoes. 0 = celula vazia. */
    fun celulas(ano: Int, mes: Int): List<List<Int>> {
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val primeiro = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val dias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val posicoes = ArrayList<Int>()
        repeat(primeiro) { posicoes.add(0) }
        for (d in 1..dias) posicoes.add(d)
        while (posicoes.size % 7 != 0) posicoes.add(0)
        return posicoes.chunked(7)
    }

    fun hojeIso(): String {
        val cal = Calendar.getInstance()
        return String.format(
            Locale.US, "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
