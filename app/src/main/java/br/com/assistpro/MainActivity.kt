package br.com.assistpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: BoletoListAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var vazio: View
    private lateinit var resumo: TextView
    private lateinit var carregador: Carregador
    private lateinit var dashPeriodo: TextView
    private lateinit var dashGanhos: TextView
    private lateinit var dashPerdas: TextView
    private lateinit var dashSaldo: TextView
    private lateinit var dashAberto: TextView
    private val repo by lazy { BoletoRepository(this) }
    private val repoGanhos by lazy { GanhoRepository(this) }
    private var apkPendente: File? = null
    private var primeiraCarga = true
    private var boletos: List<Boleto> = emptyList()
    private var ganhos: List<Ganho> = emptyList()
    private var anoFiltro = 0
    private var mesFiltro = 0
    private var diaFiltro: Int? = null

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
        dashPeriodo = findViewById(R.id.dash_periodo)
        dashGanhos = findViewById(R.id.dash_ganhos)
        dashPerdas = findViewById(R.id.dash_perdas)
        dashSaldo = findViewById(R.id.dash_saldo)
        dashAberto = findViewById(R.id.dash_aberto)
        carregador = Carregador(findViewById(R.id.carregando))

        val agora = Calendar.getInstance()
        anoFiltro = agora.get(Calendar.YEAR)
        mesFiltro = agora.get(Calendar.MONTH) + 1

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

        findViewById<ImageButton>(R.id.btn_config).setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_filtro).setOnClickListener { abrirFiltro() }
        dashPeriodo.setOnClickListener { abrirFiltro() }
        findViewById<Button>(R.id.btn_add_ganho).setOnClickListener { perguntarGanho() }

        findViewById<ImageButton>(R.id.btn_atualizar).setOnClickListener {
            verificarAtualizacao(manual = true)
        }
        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Principal")
        verificarAtualizacao(manual = false)
    }

    override fun onResume() {
        super.onResume()
        recarregar()
        ModoDev.aplicar(this, findViewById(R.id.btn_dev), "Principal")
        if (apkPendente != null) instalarPendente()
    }

    private fun recarregar() {
        carregador.iniciar(if (primeiraCarga) 0L else 1000L)
        Thread { carregar() }.start()
    }

    private fun carregar() {
        val listaBoletos = repo.listar()
        for (b in listaBoletos) BoletoNotificacoes.agendar(this, b)
        val listaGanhos = repoGanhos.listar()
        val itens = ListItem.agrupar(listaBoletos)
        val emAberto = itens.filterIsInstance<ListItem.Item>()
            .filterNot { it.boleto.pago }
            .sumOf { it.boleto.valorCentavos }
        runOnUiThread {
            boletos = listaBoletos
            ganhos = listaGanhos
            adapter.submit(itens)
            val temItens = itens.isNotEmpty()
            vazio.visibility = if (temItens) View.GONE else View.VISIBLE
            recycler.visibility = if (temItens) View.VISIBLE else View.GONE
            resumo.text = "Em aberto: ${Formato.moeda(emAberto)}"
            renderDash()
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

    private fun abrirFiltro() {
        DialogoCalendario.filtrarPeriodo(this, anoFiltro, mesFiltro, diaFiltro) { ano, mes, dia ->
            anoFiltro = ano
            mesFiltro = mes
            diaFiltro = dia
            renderDash()
        }
    }

    private fun renderDash() {
        val hoje = CalendarioMes.hojeIso()
        val dia = diaFiltro
        val f = if (dia != null) {
            Relatorio.financeiroDoDia(ganhos, boletos, anoFiltro, mesFiltro, dia)
        } else {
            Relatorio.financeiro(ganhos, boletos, anoFiltro, mesFiltro)
        }
        val r = if (dia != null) {
            Relatorio.resumoDoDia(boletos, anoFiltro, mesFiltro, dia, hoje)
        } else {
            Relatorio.resumo(boletos, anoFiltro, mesFiltro, hoje)
        }
        dashPeriodo.text = if (dia != null) {
            String.format("Dia %02d/%02d/%04d", dia, mesFiltro, anoFiltro)
        } else {
            Formato.mesTitulo(String.format("%04d-%02d", anoFiltro, mesFiltro))
        }
        dashGanhos.text = getString(R.string.dashboard_ganhos, Formato.moeda(f.ganhos))
        dashPerdas.text = getString(R.string.dashboard_perdas, Formato.moeda(f.perdas))
        dashSaldo.text = getString(R.string.dashboard_saldo, Formato.moeda(f.saldo))
        dashAberto.text = getString(R.string.dashboard_aberto, Formato.moeda(r.emAberto))
    }

    private fun perguntarGanho() {
        val view = layoutInflater.inflate(R.layout.dialog_ganho, null)
        val campoDescricao = view.findViewById<EditText>(R.id.ganho_descricao)
        val campoValor = view.findViewById<EditText>(R.id.ganho_valor)
        val campoData = view.findViewById<TextView>(R.id.ganho_data)
        val dia = diaFiltro
        val data = if (dia != null) {
            String.format("%04d-%02d-%02d", anoFiltro, mesFiltro, dia)
        } else {
            CalendarioMes.hojeIso()
        }
        campoData.text = getString(R.string.ganho_data_em, Formato.dataBrDeIso(data))

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.ganho_titulo)
            .setView(view)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.checkpoint_salvar) { _, _ ->
                val valor = Formato.parseMoeda(campoValor.text.toString())
                if (valor == null || valor <= 0L) {
                    Toast.makeText(this, R.string.ganho_valor_invalido, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val ganho = Ganho(
                    descricao = campoDescricao.text.toString().trim(),
                    valorCentavos = valor,
                    data = data
                )
                Thread {
                    repoGanhos.inserir(ganho)
                    runOnUiThread { recarregar() }
                }.start()
            }
            .show()
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
