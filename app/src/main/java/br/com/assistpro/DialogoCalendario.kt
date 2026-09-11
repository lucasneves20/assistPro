package br.com.assistpro

import android.app.Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Mostra o calendario proprio como modal (popover).
 * - escolherData: para campos de data (ex.: vencimento do boleto).
 * - filtrarPeriodo: para filtrar um mes ou um dia (ex.: dashboard da tela inicial).
 */
object DialogoCalendario {

    fun escolherData(
        activity: Activity,
        ano: Int,
        mes: Int,
        dia: Int?,
        aoEscolher: (ano: Int, mes: Int, dia: Int) -> Unit
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_calendario, null)
        val calendario = view.findViewById<CalendarioMesView>(R.id.calendario_dialogo)
        calendario.definirMes(ano, mes)
        calendario.selecionar(dia)
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.escolher_data)
            .setView(view)
            .setNegativeButton(R.string.cancelar, null)
            .create()
        calendario.aoSelecionarDia = { d ->
            aoEscolher(calendario.ano(), calendario.mes(), d)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun filtrarPeriodo(
        activity: Activity,
        ano: Int,
        mes: Int,
        dia: Int?,
        aoAplicar: (ano: Int, mes: Int, dia: Int?) -> Unit
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_calendario, null)
        val calendario = view.findViewById<CalendarioMesView>(R.id.calendario_dialogo)
        calendario.definirMes(ano, mes)
        calendario.selecionar(dia)
        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.filtrar_periodo)
            .setView(view)
            .setNegativeButton(R.string.cancelar, null)
            .setNeutralButton(R.string.todo_o_mes) { _, _ ->
                aoAplicar(calendario.ano(), calendario.mes(), null)
            }
            .create()
        calendario.aoSelecionarDia = { d ->
            aoAplicar(calendario.ano(), calendario.mes(), d)
            dialog.dismiss()
        }
        dialog.show()
    }
}
