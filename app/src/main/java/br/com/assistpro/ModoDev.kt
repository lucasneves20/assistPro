package br.com.assistpro

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Modo desenvolvedor: liga um botao universal (no header de cada tela) que
 * grava um checkpoint do estado atual na tabela "checkpoints" e o exporta
 * para Download/assistpro_checkpoints.json.
 */
object ModoDev {

    private const val PREFS = "config"
    private const val CHAVE = "modo_dev"

    fun ativo(context: Context): Boolean =
        prefs(context).getBoolean(CHAVE, false)

    fun definir(context: Context, ativo: Boolean) {
        prefs(context).edit().putBoolean(CHAVE, ativo).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun aplicar(activity: Activity, botao: View, tela: String) {
        botao.visibility = if (ativo(activity)) View.VISIBLE else View.GONE
        botao.setOnClickListener { capturar(activity, tela) }
    }

    fun capturar(activity: Activity, tela: String) {
        LogApp.i("Checkpoint solicitado em $tela")
        Toast.makeText(activity, R.string.checkpoint_salvando, Toast.LENGTH_SHORT).show()
        Thread {
            val boletos = BoletoRepository(activity).listar()
            val emAberto = boletos.filterNot { it.pago }.sumOf { it.valorCentavos }
            val checkpoint = ColetorCheckpoint.coletar(activity, tela, boletos.size, emAberto)
            CheckpointRepository(activity).inserir(checkpoint)
            val arquivo = CheckpointRepository.exportar(activity)
            activity.runOnUiThread {
                val msg = if (arquivo != null) {
                    activity.getString(R.string.checkpoint_salvo, arquivo.absolutePath)
                } else {
                    activity.getString(R.string.checkpoint_salvo_banco)
                }
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
            }
        }.start()
    }
}
