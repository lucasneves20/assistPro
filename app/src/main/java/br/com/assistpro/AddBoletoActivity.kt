package br.com.assistpro

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class AddBoletoActivity : AppCompatActivity() {

    private val repo by lazy { BoletoRepository(this) }

    private lateinit var preview: ImageView
    private lateinit var campoDescricao: EditText
    private lateinit var campoLinha: EditText
    private lateinit var campoValor: EditText
    private lateinit var campoVencimento: EditText
    private lateinit var status: TextView
    private lateinit var titulo: TextView

    private var boletoId: Long = 0L
    private var boletoAtual: Boleto? = null
    private var bitmap: Bitmap? = null
    private var fotoUri: Uri? = null
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) processarUri(fotoUri) }

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) processarUri(uri) }

    private val pickArquivo = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == RESULT_OK) {
            val caminho = resultado.data?.getStringExtra(FileExplorerActivity.EXTRA_PATH)
            if (caminho != null) processarCaminho(caminho)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        preview = findViewById(R.id.preview)
        campoDescricao = findViewById(R.id.campo_descricao)
        campoLinha = findViewById(R.id.campo_linha)
        campoValor = findViewById(R.id.campo_valor)
        campoVencimento = findViewById(R.id.campo_vencimento)
        status = findViewById(R.id.status)
        titulo = findViewById(R.id.titulo_add)

        boletoId = intent.getLongExtra(EXTRA_ID, 0L)
        if (boletoId > 0) {
            titulo.setText(R.string.editar)
            carregarBoleto()
        }

        findViewById<Button>(R.id.btn_foto).setOnClickListener { tirarFoto() }
        findViewById<Button>(R.id.btn_galeria).setOnClickListener { pickImage.launch("image/*") }
        findViewById<Button>(R.id.btn_arquivos).setOnClickListener {
            pickArquivo.launch(Intent(this, FileExplorerActivity::class.java))
        }
        findViewById<Button>(R.id.btn_salvar).setOnClickListener { salvar() }

        campoVencimento.setOnClickListener { escolherData() }
        campoVencimento.isFocusable = false
    }

    private fun tirarFoto() {
        val dir = File(cacheDir, "capturas")
        if (!dir.exists()) dir.mkdirs()
        val arquivo = File(dir, "boleto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", arquivo)
        fotoUri = uri
        takePicture.launch(uri)
    }

    private fun processarUri(uri: Uri?) {
        if (uri == null) return
        val bmp = carregarBitmap(uri)
        if (bmp == null) {
            status.text = "Nao foi possivel abrir a imagem."
            return
        }
        bitmap = bmp
        preview.setImageBitmap(bmp)
        rodarOcr(bmp)
    }

    private fun processarCaminho(caminho: String) {
        val arquivo = File(caminho)
        if (!arquivo.exists()) {
            status.text = "Arquivo nao encontrado."
            return
        }
        val ext = arquivo.extension.lowercase(Locale.ROOT)
        val bmp = if (ext == "pdf") renderizarPdf(arquivo) else decodificarArquivo(arquivo)
        if (bmp == null) {
            status.text = "Nao foi possivel abrir o arquivo."
            return
        }
        bitmap = bmp
        preview.setImageBitmap(bmp)
        rodarOcr(bmp)
    }

    private fun carregarBitmap(uri: Uri): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val opts = BitmapFactory.Options().apply { inSampleSize = calcularSample(bounds) }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodificarArquivo(arquivo: File): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(arquivo.absolutePath, bounds)
            val opts = BitmapFactory.Options().apply { inSampleSize = calcularSample(bounds) }
            BitmapFactory.decodeFile(arquivo.absolutePath, opts)
        } catch (e: Exception) {
            null
        }
    }

    private fun calcularSample(bounds: BitmapFactory.Options): Int {
        var sample = 1
        val maior = maxOf(bounds.outWidth, bounds.outHeight)
        while (maior / sample > 1800) sample *= 2
        return sample
    }

    private fun renderizarPdf(arquivo: File): Bitmap? {
        return try {
            val pfd = ParcelFileDescriptor.open(arquivo, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pagina = renderer.openPage(0)
            val largura = pagina.width
            val altura = pagina.height
            val escala = if (maxOf(largura, altura) > 1800) 1800f / maxOf(largura, altura) else 1f
            val bmp = Bitmap.createBitmap(
                (largura * escala).toInt().coerceAtLeast(1),
                (altura * escala).toInt().coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            bmp.eraseColor(Color.WHITE)
            pagina.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pagina.close()
            renderer.close()
            pfd.close()
            bmp
        } catch (e: Exception) {
            null
        }
    }

    private fun rodarOcr(bmp: Bitmap) {
        status.text = "Lendo o boleto..."
        recognizer.process(InputImage.fromBitmap(bmp, 0))
            .addOnSuccessListener { texto ->
                preencher(texto.text)
                status.text = "Leitura concluida. Confira os campos."
            }
            .addOnFailureListener {
                status.text = "Falha na leitura. Preencha manualmente."
            }
    }

    private fun preencher(texto: String) {
        val r = BoletoParser.parse(texto)
        r.linha?.let { campoLinha.setText(it) }
        r.valorCentavos?.let { if (it > 0) campoValor.setText(Formato.moeda(it)) }
        r.vencimentoIso?.let { campoVencimento.setText(Formato.dataBrDeIso(it)) }
    }

    private fun escolherData() {
        val cal = Calendar.getInstance()
        val iso = Formato.isoDeDataBr(campoVencimento.text.toString())
        if (iso != null) {
            val p = iso.split("-")
            cal.set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt())
        }
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                campoVencimento.setText(
                    String.format(Locale.US, "%02d/%02d/%04d", dia, mes + 1, ano)
                )
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun salvar() {
        val linha = BoletoParser.onlyDigits(campoLinha.text.toString())
        val valor = Formato.parseMoeda(campoValor.text.toString()) ?: 0L
        val vencTexto = campoVencimento.text.toString().trim()
        val vencIso = if (vencTexto.isEmpty()) null else Formato.isoDeDataBr(vencTexto)

        if (linha.isEmpty() && valor == 0L && vencIso == null) {
            status.text = "Informe ao menos a linha digitavel, o valor ou o vencimento."
            return
        }
        if (vencTexto.isNotEmpty() && vencIso == null) {
            status.text = "Data de vencimento invalida."
            return
        }

        val imagem = salvarImagem() ?: boletoAtual?.imagem
        val boleto = (boletoAtual ?: Boleto()).copy(
            linha = linha,
            valorCentavos = valor,
            vencimento = vencIso,
            descricao = campoDescricao.text.toString().trim(),
            imagem = imagem
        )

        Thread {
            if (boletoId > 0) {
                repo.atualizar(boleto)
            } else {
                boleto.id = repo.inserir(boleto)
            }
            BoletoNotificacoes.agendar(this, boleto)
            runOnUiThread { finish() }
        }.start()
    }

    private fun carregarBoleto() {
        if (boletoId <= 0) return
        Thread {
            val b = repo.buscar(boletoId) ?: return@Thread
            val bmp = b.imagem?.let { caminho ->
                val arquivo = File(caminho)
                if (arquivo.exists()) BitmapFactory.decodeFile(arquivo.absolutePath) else null
            }
            runOnUiThread {
                boletoAtual = b
                campoDescricao.setText(b.descricao)
                campoLinha.setText(b.linha)
                if (b.valorCentavos > 0) campoValor.setText(Formato.moeda(b.valorCentavos))
                b.vencimento?.let { campoVencimento.setText(Formato.dataBrDeIso(it)) }
                if (bmp != null) preview.setImageBitmap(bmp)
            }
        }.start()
    }

    private fun salvarImagem(): String? {
        val bmp = bitmap ?: return null
        return try {
            val dir = File(filesDir, "boletos")
            if (!dir.exists()) dir.mkdirs()
            val arquivo = File(dir, "boleto_${System.currentTimeMillis()}.jpg")
            FileOutputStream(arquivo).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            arquivo.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.close()
    }

    companion object {
        const val EXTRA_ID = "boleto_id"
    }
}
