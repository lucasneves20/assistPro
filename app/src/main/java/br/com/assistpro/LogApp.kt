package br.com.assistpro

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Log em memoria mantido pelo proprio app.
 * O Android nao permite ler o logcat sem privilegio, entao guardamos as
 * ultimas mensagens para anexar aos checkpoints do modo desenvolvedor.
 */
object LogApp {

    private const val TAG = "assistPro"
    private const val MAX = 300
    private val formato = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val linhas = ArrayDeque<String>()
    private var ultimoErro: String? = null

    @Synchronized
    fun i(mensagem: String) = registrar("I", mensagem)

    @Synchronized
    fun w(mensagem: String) = registrar("W", mensagem)

    @Synchronized
    fun e(mensagem: String, erro: Throwable? = null) {
        val texto = if (erro != null) {
            "$mensagem: ${erro.javaClass.simpleName}: ${erro.message}"
        } else {
            mensagem
        }
        ultimoErro = texto
        registrar("E", texto)
    }

    @Synchronized
    private fun registrar(nivel: String, mensagem: String) {
        linhas.addLast("${formato.format(Date())} $nivel $mensagem")
        while (linhas.size > MAX) linhas.removeFirst()
        Log.println(if (nivel == "E") Log.ERROR else Log.INFO, TAG, mensagem)
    }

    @Synchronized
    fun texto(): String = linhas.joinToString("\n")

    @Synchronized
    fun ultimoErro(): String? = ultimoErro

    @Synchronized
    fun limpar() {
        linhas.clear()
        ultimoErro = null
    }
}
