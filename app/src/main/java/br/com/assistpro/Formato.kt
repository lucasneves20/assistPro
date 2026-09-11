package br.com.assistpro

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Formato {

    private val ptBr = Locale("pt", "BR")

    fun moeda(centavos: Long): String {
        val negativo = centavos < 0
        val v = Math.abs(centavos)
        val reais = v / 100
        val cents = v % 100
        val s = reais.toString()
        val sb = StringBuilder()
        var cont = 0
        for (i in s.length - 1 downTo 0) {
            sb.append(s[i])
            cont++
            if (cont % 3 == 0 && i != 0) sb.append('.')
        }
        sb.reverse()
        return (if (negativo) "-" else "") + "R$ " + sb.toString() + "," + String.format(Locale.US, "%02d", cents)
    }

    fun parseMoeda(txt: String): Long? {
        val limpo = txt.replace("R$", "").replace(" ", "").trim()
        if (limpo.isEmpty()) return null
        val norm = if (limpo.contains(",")) {
            limpo.replace(".", "").replace(",", ".")
        } else {
            limpo
        }
        val d = norm.toDoubleOrNull() ?: return null
        return Math.round(d * 100.0)
    }

    fun mesTitulo(yyyymm: String): String {
        val partes = yyyymm.split("-")
        if (partes.size < 2) return yyyymm
        val ano = partes[0].toIntOrNull() ?: return yyyymm
        val mes = partes[1].toIntOrNull() ?: return yyyymm
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, 1)
        val fmt = SimpleDateFormat("MMMM 'de' yyyy", ptBr)
        val texto = fmt.format(cal.time)
        return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptBr) else it.toString() }
    }

    fun dataBrDeIso(iso: String?): String {
        if (iso.isNullOrEmpty()) return ""
        val partes = iso.split("-")
        if (partes.size < 3) return iso
        return "${partes[2]}/${partes[1]}/${partes[0]}"
    }

    fun isoDeDataBr(texto: String): String? {
        val m = Regex("([0-3]?\\d)[/.-]([01]?\\d)[/.-](\\d{4})").find(texto.trim()) ?: return null
        val dd = m.groupValues[1].toIntOrNull() ?: return null
        val mm = m.groupValues[2].toIntOrNull() ?: return null
        val yyyy = m.groupValues[3].toIntOrNull() ?: return null
        if (dd !in 1..31 || mm !in 1..12 || yyyy !in 1900..2099) return null
        return String.format(Locale.US, "%04d-%02d-%02d", yyyy, mm, dd)
    }
}
