package br.com.assistpro

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Verifica e baixa novas versoes publicadas como Releases no GitHub.
 * O APK precisa estar anexado ao release e o repositorio e configurado em
 * assistpro.updateRepo (gradle.properties) -> BuildConfig.UPDATE_REPO.
 */
object UpdateChecker {

    private const val API = "https://api.github.com/repos"
    private const val USER_AGENT = "assistPro-updater"

    data class Info(val versao: String, val notas: String, val apkUrl: String)

    fun buscar(): Info? {
        val conn = (URL("$API/${BuildConfig.UPDATE_REPO}/releases/latest").openConnection()
                as HttpURLConnection)
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            val codigo = conn.responseCode
            if (codigo != 200) throw IllegalStateException("GitHub respondeu $codigo")
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val versao = json.optString("tag_name").trim().removePrefix("v").removePrefix("V")
            if (versao.isEmpty()) return null
            val assets = json.optJSONArray("assets") ?: return null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name").endsWith(".apk", ignoreCase = true)) {
                    val url = a.optString("browser_download_url")
                    if (url.isNotEmpty()) {
                        return Info(versao, json.optString("body").trim(), url)
                    }
                }
            }
            return null
        } finally {
            conn.disconnect()
        }
    }

    fun maisNova(remota: String, atual: String): Boolean = comparar(remota, atual) > 0

    private fun comparar(a: String, b: String): Int {
        val pa = a.split(".", "-").mapNotNull { it.takeWhile(Char::isDigit).toIntOrNull() }
        val pb = b.split(".", "-").mapNotNull { it.takeWhile(Char::isDigit).toIntOrNull() }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val va = pa.getOrElse(i) { 0 }
            val vb = pb.getOrElse(i) { 0 }
            if (va != vb) return va - vb
        }
        return 0
    }

    fun baixar(context: Context, url: String): File {
        val dir = File(context.cacheDir, "atualizacoes")
        if (!dir.exists()) dir.mkdirs()
        dir.listFiles()?.forEach { it.delete() }
        val destino = File(dir, "assistPro.apk")
        val conn = (URL(url).openConnection() as HttpURLConnection)
        conn.connectTimeout = 15000
        conn.readTimeout = 30000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", USER_AGENT)
        try {
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("download falhou (${conn.responseCode})")
            }
            conn.inputStream.use { entrada ->
                destino.outputStream().use { saida -> entrada.copyTo(saida) }
            }
        } finally {
            conn.disconnect()
        }
        return destino
    }

    fun podeInstalar(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    fun pedirPermissaoInstalacao(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun instalar(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}
