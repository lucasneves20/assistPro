package br.com.assistpro

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Agenda o aviso de vencimento de cada boleto com o AlarmManager.
 * O aviso dispara um dia antes do vencimento, as 09:00.
 */
object BoletoNotificacoes {

    const val CANAL_ID = "vencimentos"
    const val EXTRA_ID = "boleto_id"
    const val EXTRA_DESCRICAO = "boleto_descricao"
    const val EXTRA_VALOR = "boleto_valor"
    const val EXTRA_VENCIMENTO = "boleto_vencimento"

    private const val HORA_AVISO = 9
    private const val DIAS_ANTES = 1

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CANAL_ID) != null) return
        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.notificacao_canal),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notificacao_canal_desc)
        }
        manager.createNotificationChannel(canal)
    }

    fun agendar(context: Context, boleto: Boleto) {
        val vencimento = boleto.vencimento
        if (boleto.pago || vencimento.isNullOrEmpty()) {
            cancelar(context, boleto.id)
            return
        }
        val quando = horarioAviso(vencimento)
        if (quando == null || quando <= System.currentTimeMillis()) {
            cancelar(context, boleto.id)
            return
        }
        criarCanal(context)
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = pendingIntent(context, boleto)
        val exato = try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, quando, pi)
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
        if (!exato) manager.set(AlarmManager.RTC_WAKEUP, quando, pi)
    }

    fun cancelar(context: Context, id: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, BoletoAlarmeReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.cancel(pi)
    }

    private fun pendingIntent(context: Context, boleto: Boleto): PendingIntent {
        val intent = Intent(context, BoletoAlarmeReceiver::class.java).apply {
            putExtra(EXTRA_ID, boleto.id)
            putExtra(EXTRA_DESCRICAO, boleto.descricao)
            putExtra(EXTRA_VALOR, boleto.valorCentavos)
            putExtra(EXTRA_VENCIMENTO, boleto.vencimento)
        }
        return PendingIntent.getBroadcast(
            context, boleto.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun horarioAviso(iso: String): Long? {
        val cal = parseIso(iso) ?: return null
        cal.add(Calendar.DAY_OF_MONTH, -DIAS_ANTES)
        cal.set(Calendar.HOUR_OF_DAY, HORA_AVISO)
        return cal.timeInMillis
    }

    private fun parseIso(iso: String): Calendar? {
        val partes = iso.split("-")
        if (partes.size < 3) return null
        val ano = partes[0].toIntOrNull() ?: return null
        val mes = partes[1].toIntOrNull() ?: return null
        val dia = partes[2].toIntOrNull() ?: return null
        val cal = Calendar.getInstance()
        cal.set(ano, mes - 1, dia, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
}
