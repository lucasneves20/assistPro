package br.com.assistpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileExplorerActivity : AppCompatActivity() {

    private lateinit var adapter: FileAdapter
    private lateinit var caminhoView: TextView
    private lateinit var vazio: TextView
    private lateinit var carregador: Carregador
    private var atual: File = Environment.getExternalStorageDirectory()

    private val pedirTudo = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { recarregarSePermitido() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_explorer)

        caminhoView = findViewById(R.id.caminho)
        vazio = findViewById(R.id.vazio)
        carregador = Carregador(findViewById(R.id.carregando))
        val recycler = findViewById<RecyclerView>(R.id.lista)

        adapter = FileAdapter { entry -> abrir(entry) }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<ImageButton>(R.id.btn_up).setOnClickListener { subir() }

        if (temPermissao()) listar(atual) else pedirPermissao()
    }

    override fun onResume() {
        super.onResume()
        if (temPermissao()) listar(atual)
    }

    private fun temPermissao(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun pedirPermissao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                pedirTudo.launch(intent)
            } catch (e: Exception) {
                pedirTudo.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CODIGO_PERMISSAO
            )
        }
    }

    private fun recarregarSePermitido() {
        if (temPermissao()) listar(atual)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (temPermissao()) listar(atual)
    }

    private fun abrir(entry: FileEntry) {
        if (entry.diretorio) {
            atual = entry.file
            listar(atual)
        } else {
            val data = Intent().putExtra(EXTRA_PATH, entry.file.absolutePath)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    private fun subir() {
        val raiz = Environment.getExternalStorageDirectory()
        val parent = atual.parentFile
        val podeSubir = parent != null &&
            atual.absolutePath != raiz.absolutePath &&
            atual.absolutePath.startsWith(raiz.absolutePath)
        atual = if (podeSubir) parent!! else raiz
        listar(atual)
    }

    private fun listar(dir: File) {
        caminhoView.text = dir.absolutePath
        carregador.iniciar()
        Thread {
            val entradas = (dir.listFiles() ?: emptyArray())
                .filter { !it.name.startsWith(".") && FileAdapter.imagemOuPdf(it) }
                .sortedWith(
                    compareByDescending<File> { it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )
                .map { FileEntry(it, it.isDirectory) }
            runOnUiThread {
                adapter.submit(entradas)
                vazio.visibility = if (entradas.isEmpty()) View.VISIBLE else View.GONE
                carregador.finalizar()
            }
        }.start()
    }

    companion object {
        const val EXTRA_PATH = "caminho_arquivo"
        private const val CODIGO_PERMISSAO = 100
    }
}
