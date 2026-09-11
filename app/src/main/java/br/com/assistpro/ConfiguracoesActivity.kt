package br.com.assistpro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch

class ConfiguracoesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Configurações")

        val switch = findViewById<MaterialSwitch>(R.id.switch_dev)
        switch.isChecked = ModoDev.ativo(this)
        switch.setOnCheckedChangeListener { _, marcado ->
            ModoDev.definir(this, marcado)
            Toast.makeText(
                this,
                if (marcado) R.string.modo_dev_ativado else R.string.modo_dev_desativado,
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<Button>(R.id.btn_exportar_checkpoints).setOnClickListener {
            Thread {
                val arquivo = CheckpointRepository.exportar(this)
                runOnUiThread {
                    val msg = if (arquivo != null) {
                        getString(R.string.exportado_para, arquivo.absolutePath)
                    } else {
                        getString(R.string.exportar_falhou)
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                }
            }.start()
        }

        findViewById<Button>(R.id.btn_limpar_checkpoints).setOnClickListener {
            Thread {
                val repo = CheckpointRepository(this)
                val antes = repo.contar()
                repo.limpar()
                runOnUiThread {
                    Toast.makeText(
                        this,
                        getString(R.string.checkpoints_removidos, antes),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.start()
        }

        findViewById<Button>(R.id.btn_dados_exemplo).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dados_exemplo)
                .setMessage(R.string.dados_exemplo_confirmar)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.checkpoint_salvar) { _, _ ->
                    Thread {
                        val total = DadosExemplo.gerar(this)
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                getString(R.string.dados_exemplo_ok, total),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.start()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Configurações")
    }
}
