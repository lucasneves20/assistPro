package br.com.assistpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: BoletoListAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var vazio: View
    private lateinit var resumo: TextView
    private lateinit var carregador: Carregador
    private val repo by lazy { BoletoRepository(this) }
    private var apkPendente: File? = null
    private var primeiraCarga = true

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { recarregar() }

    private val pedirNotificacoes = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BoletoNotificacoes.criarCanal(this)
        pedirPermissaoNotificacoes()

        recycler = findViewById(R.id.lista)
        vazio = findViewById(R.id.vazio)
        resumo = findViewById(R.id.resumo)
        carregador = Carregador(findViewById(R.id.carregando))

        adapter = BoletoListAdapter(
            onEdit = { b -> abrirEdicao(b) },
            onToggle = { b -> alternarPago(b) },
            onDelete = { b -> confirmarExclusao(b) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            addLauncher.launch(Intent(this, AddBoletoActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_atualizar).setOnClickListener {
            verificarAtualizacao(manual = true)
        }
        verificarAtualizacao(manual = false)
    }

    override fun onResume() {
        super.onResume()
        recarregar()
        if (apkPendente != null) instalarPendente()
    }

    private fun recarregar() {
        carregador.iniciar(if (primeiraCarga) 0L else 1000L)
        Thread { carregar() }.start()
    }

    private fun carregar() {
        val boletos = repo.listar()
        for (b in boletos) BoletoNotificacoes.agendar(this, b)
        val itens = ListItem.agrupar(boletos)
        val emAberto = itens.filterIsInstance<ListItem.Item>()
            .filterNot { it.boleto.pago }
            .sumOf { it.boleto.valorCentavos }
        runOnUiThread {
            adapter.submit(itens)
            val temItens = itens.isNotEmpty()
            vazio.visibility = if (temItens) View.GONE else View.VISIBLE
            recycler.visibility = if (temItens) View.VISIBLE else View.GONE
            resumo.text = "Em aberto: ${Formato.moeda(emAberto)}"
            carregador.finalizar()
            primeiraCarga = false
        }
    }

    private fun pedirPermissaoNotificacoes() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val concedida = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedida) pedirNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun verificarAtualizacao(manual: Boolean) {
        val prefs = getSharedPreferences("atualizacao", MODE_PRIVATE)
        if (!manual &&
            System.currentTimeMillis() - prefs.getLong("ultima_verificacao", 0L) < INTERVALO
        ) {
            return
        }
        prefs.edit().putLong("ultima_verificacao", System.currentTimeMillis()).apply()
        if (manual) aviso(R.string.atualizacao_verificando)

        Thread {
            val info = try {
                UpdateChecker.buscar()
            } catch (e: Exception) {
                if (manual) aviso(R.string.atualizacao_falha)
                return@Thread
            }
            if (info == null) {
                if (manual) aviso(R.string.atualizacao_falha)
                return@Thread
            }
            if (!UpdateChecker.maisNova(info.versao, BuildConfig.VERSION_NAME)) {
                if (manual) aviso(R.string.atualizacao_atual)
                return@Thread
            }
            runOnUiThread { mostrarAtualizacao(info) }
        }.start()
    }

    private fun mostrarAtualizacao(info: UpdateChecker.Info) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.atualizacao_titulo, info.versao))
            .setMessage(info.notas.ifBlank { getString(R.string.atualizacao_notas) })
            .setNegativeButton(R.string.atualizacao_depois, null)
            .setPositiveButton(R.string.atualizacao_instalar) { _, _ -> baixarEInstalar(info) }
            .show()
    }

    private fun baixarEInstalar(info: UpdateChecker.Info) {
        aviso(R.string.atualizacao_baixando)
        Thread {
            val apk = try {
                UpdateChecker.baixar(this, info.apkUrl)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                if (apk == null) {
                    aviso(R.string.atualizacao_erro_download)
                } else {
                    apkPendente = apk
                    instalarPendente()
                }
            }
        }.start()
    }

    private fun instalarPendente() {
        val apk = apkPendente ?: return
        if (!UpdateChecker.podeInstalar(this)) {
            UpdateChecker.pedirPermissaoInstalacao(this)
            return
        }
        apkPendente = null
        UpdateChecker.instalar(this, apk)
    }

    private fun aviso(res: Int) {
        runOnUiThread { Toast.makeText(this, res, Toast.LENGTH_SHORT).show() }
    }

    private fun alternarPago(b: Boleto) {
        Thread {
            repo.atualizarPago(b.id, !b.pago)
            carregar()
        }.start()
    }

    private fun abrirEdicao(b: Boleto) {
        val i = Intent(this, AddBoletoActivity::class.java)
        i.putExtra(AddBoletoActivity.EXTRA_ID, b.id)
        addLauncher.launch(i)
    }

    private fun confirmarExclusao(b: Boleto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir boleto")
            .setMessage("Deseja remover este boleto?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                Thread {
                    repo.remover(b.id)
                    carregar()
                }.start()
            }
            .show()
    }

    companion object {
        private const val INTERVALO = 6 * 60 * 60 * 1000L
    }
}
