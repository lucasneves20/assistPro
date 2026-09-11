package br.com.assistpro

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.TimeZone

/**
 * Monta um Checkpoint com o estado do app, variaveis do sistema/dispositivo,
 * ultimo erro e as ultimas linhas de log.
 */
object ColetorCheckpoint {

    fun coletar(
        context: Context,
        tela: String,
        qtdBoletos: Int,
        emAbertoCentavos: Long,
        observacao: String,
        tipo: String
    ): Checkpoint {
        val versao = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
        val erro = LogApp.ultimoErro()
        return Checkpoint(
            tela = tela,
            versaoApp = versao,
            temErro = erro != null,
            ultimoErro = erro,
            app = appJson(ModoDev.ativo(context), qtdBoletos, emAbertoCentavos, versao),
            sistema = sistemaJson(context),
            logs = LogApp.texto(),
            observacao = observacao,
            tipo = tipo
        )
    }

    fun appJson(
        modoDev: Boolean,
        qtdBoletos: Int,
        emAbertoCentavos: Long,
        versaoApp: String
    ): String {
        val o = JSONObject()
        o.put("modoDev", modoDev)
        o.put("qtdBoletos", qtdBoletos)
        o.put("emAbertoCentavos", emAbertoCentavos)
        o.put("versaoApp", versaoApp)
        o.put("pacote", BuildConfig.APPLICATION_ID)
        return o.toString()
    }

    fun sistemaJson(context: Context): String {
        val o = JSONObject()
        o.put("fabricante", Build.MANUFACTURER)
        o.put("modelo", Build.MODEL)
        o.put("dispositivo", Build.DEVICE)
        o.put("hardware", Build.HARDWARE)
        o.put("android", Build.VERSION.RELEASE)
        o.put("sdk", Build.VERSION.SDK_INT)
        o.put("abis", JSONArray(Build.SUPPORTED_ABIS.toList()))
        o.put("locale", Locale.getDefault().toString())
        o.put("fuso", TimeZone.getDefault().id)
        try {
            val m = context.resources.displayMetrics
            o.put("tela", JSONObject().apply {
                put("larguraPx", m.widthPixels)
                put("alturaPx", m.heightPixels)
                put("densidade", m.density.toDouble())
                put("densityDpi", m.densityDpi)
            })
            val r = Runtime.getRuntime()
            o.put("memoria", JSONObject().apply {
                put("maxBytes", r.maxMemory())
                put("totalBytes", r.totalMemory())
                put("livreBytes", r.freeMemory())
            })
            o.put("armazenamentoLivreBytes", File(context.filesDir.absolutePath).usableSpace)
            o.put("uptimeMs", SystemClock.elapsedRealtime())
            val bateria = ContextCompat.registerReceiver(
                context,
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            if (bateria != null) {
                o.put("bateria", JSONObject().apply {
                    put("nivel", bateria.getIntExtra(BatteryManager.EXTRA_LEVEL, -1))
                    put("escala", bateria.getIntExtra(BatteryManager.EXTRA_SCALE, -1))
                    put(
                        "carregando",
                        bateria.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ==
                            BatteryManager.BATTERY_STATUS_CHARGING
                    )
                })
            }
        } catch (e: Exception) {
            o.put("erroColeta", e.message ?: "erro")
        }
        return o.toString()
    }
}
