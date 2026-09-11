package br.com.assistpro

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

/**
 * Modo desenvolvedor: liga um botao universal (no header de cada tela) que abre
 * um dialogo para descrever um bug ou deixar um feedback e grava um checkpoint
 * do estado atual na tabela "checkpoints", exportando tambem para
 * Download/assistpro_checkpoints.json.
 */
object ModoDev {

    private const val PREFS = "config"
    private const val CHAVE = "modo_dev"

    const val TIPO_BUG = "bug"
    const val TIPO_FEEDBACK = "feedback"

    fun ativo(context: Context): Boolean =
        prefs(context).getBoolean(CHAVE, false)

    fun definir(context: Context, ativo: Boolean) {
        prefs(context).edit().putBoolean(CHAVE, ativo).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Tipo do checkpoint: "bug" quando marcado, "feedback" caso contrario. */
    fun tipo(ehBug: Boolean): String = if (ehBug) TIPO_BUG else TIPO_FEEDBACK

    fun aplicar(activity: Activity, botao: View, tela: String) {
        botao.visibility = if (ativo(activity)) View.VISIBLE else View.GONE
        botao.setOnClickListener { perguntar(activity, tela) }
    }

    private fun perguntar(activity: Activity, tela: String) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_checkpoint, null)
        val campo = view.findViewById<EditText>(R.id.campo_observacao)
        val switch = view.findViewById<MaterialSwitch>(R.id.switch_bug)
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.checkpoint_titulo)
            .setView(view)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.checkpoint_salvar) { _, _ ->
                salvar(activity, tela, campo.text.toString().trim(), tipo(switch.isChecked))
            }
            .show()
    }

    private fun salvar(activity: Activity, tela: String, observacao: String, tipo: String) {
        LogApp.i("Checkpoint ($tipo) em $tela: $observacao")
        Toast.makeText(activity, R.string.checkpoint_salvando, Toast.LENGTH_SHORT).show()
        Thread {
            val boletos = BoletoRepository(activity).listar()
            val emAberto = boletos.filterNot { it.pago }.sumOf { it.valorCentavos }
            val checkpoint = ColetorCheckpoint.coletar(
                activity, tela, boletos.size, emAberto, observacao, tipo
            )
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
