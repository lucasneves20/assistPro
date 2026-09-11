package br.com.assistpro

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Le e interpreta a linha digitavel de boletos brasileiros.
 *
 * Bancario: 47 digitos, sendo o campo 5 (14 digitos) = fator de vencimento (4) + valor (10).
 * Arrecadacao/convenio: 48 digitos, comeca com 8.
 */
object BoletoParser {

    data class Result(
        val linha: String?,
        val valorCentavos: Long?,
        val vencimentoIso: String?,
        val descricao: String?
    )

    val isoFmt: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val linhaRegex = Regex(
        "(\\d{5})\\.?(\\d{5})\\s?(\\d{5})\\.?(\\d{6})\\s?(\\d{5})\\.?(\\d{6})\\s?(\\d)\\s?(\\d{14})"
    )

    fun onlyDigits(s: String): String = s.filter { it.isDigit() }

    fun findLinha(text: String): String? {
        val m = linhaRegex.find(text)
        if (m != null) {
            val sb = StringBuilder()
            for (i in 1..8) sb.append(m.groupValues[i])
            val d = sb.toString()
            if (d.length == 47) return d
        }
        for (line in text.split('\n')) {
            val d = onlyDigits(line)
            if (d.length == 47 || d.length == 48) return d
        }
        for (line in text.split('\n')) {
            val d = onlyDigits(line)
            if (d.length >= 44 && d.length <= 48) return d
        }
        return null
    }

    /** Retorna (valor em centavos, vencimento ISO) a partir da linha digitavel. */
    fun parseLinha(linha: String): Pair<Long?, String?> {
        val d = onlyDigits(linha)
        if (d.length == 47) {
            val campo5 = d.substring(33, 47)
            val valor = campo5.substring(4).toLongOrNull()
            val fator = campo5.substring(0, 4).toIntOrNull() ?: 0
            return valor to fatorParaVencimento(fator)
        }
        if (d.length == 48 && d.startsWith("8")) {
            val valor = d.substring(4, 15).toLongOrNull()
            return valor to vencimentoArrecadacao(d)
        }
        return null to null
    }

    private fun fatorParaVencimento(fator: Int): String? {
        if (fator <= 0) return null
        val cal = Calendar.getInstance()
        if (fator >= 1000) {
            cal.set(2025, Calendar.FEBRUARY, 22, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_MONTH, fator - 1000)
        } else {
            cal.set(1997, Calendar.OCTOBER, 7, 0, 0, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_MONTH, fator)
        }
        return isoFmt.format(cal.time)
    }

    private fun vencimentoArrecadacao(d: String): String? {
        if (d.length < 27) return null
        val yyyymmdd = d.substring(19, 27)
        return try {
            val df = SimpleDateFormat("yyyyMMdd", Locale.US)
            df.isLenient = false
            val date = df.parse(yyyymmdd) ?: return null
            val year = yyyymmdd.substring(0, 4).toInt()
            if (year in 1990..2099) isoFmt.format(date) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseBrValor(s: String): Long? {
        val clean = s.trim().replace(".", "").replace(",", ".")
        val d = clean.toDoubleOrNull() ?: return null
        return Math.round(d * 100.0)
    }

    fun findValor(text: String): Long? {
        val m = Regex("[0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2}").find(text) ?: return null
        return parseBrValor(m.value)
    }

    fun findVencimento(text: String): String? {
        val idx = text.lowercase(Locale.ROOT).indexOf("venc")
        val scope = if (idx >= 0) text.substring(idx) else text
        val m = Regex("([0-3]?\\d)[/.]\\s?([01]?\\d)[/.]\\s?(\\d{4})").find(scope) ?: return null
        val dd = m.groupValues[1].toIntOrNull() ?: return null
        val mm = m.groupValues[2].toIntOrNull() ?: return null
        val yyyy = m.groupValues[3].toIntOrNull() ?: return null
        if (dd !in 1..31 || mm !in 1..12 || yyyy !in 1990..2099) return null
        val cal = Calendar.getInstance()
        cal.set(yyyy, mm - 1, dd, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return isoFmt.format(cal.time)
    }

    fun parse(text: String): Result {
        val linha = findLinha(text)
        var valor: Long? = null
        var venc: String? = null
        if (linha != null) {
            val (v, d) = parseLinha(linha)
            valor = v
            venc = d
        }
        if (valor == null || valor == 0L) valor = findValor(text)
        if (venc == null) venc = findVencimento(text)
        return Result(linha, valor, venc, null)
    }
}
