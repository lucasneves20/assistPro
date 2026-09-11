package br.com.assistpro

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    }

    override fun onResume() {
        super.onResume()
        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Configurações")
    }
}
