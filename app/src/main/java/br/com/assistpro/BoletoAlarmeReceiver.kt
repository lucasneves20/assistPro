package br.com.assistpro

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class BoletoAlarmeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(BoletoNotificacoes.EXTRA_ID, 0L)
        val descricao = intent.getStringExtra(BoletoNotificacoes.EXTRA_DESCRICAO).orEmpty()
        val valor = intent.getLongExtra(BoletoNotificacoes.EXTRA_VALOR, 0L)
        val vencimento = intent.getStringExtra(BoletoNotificacoes.EXTRA_VENCIMENTO)

        BoletoNotificacoes.criarCanal(context)

        val abrir = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val abre = PendingIntent.getActivity(
            context, id.toInt(), abrir,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nome = if (descricao.isNotBlank()) descricao else context.getString(R.string.boleto)
        val texto = if (vencimento.isNullOrEmpty()) {
            Formato.moeda(valor)
        } else {
            context.getString(
                R.string.notificacao_texto,
                Formato.dataBrDeIso(vencimento),
                Formato.moeda(valor)
            )
        }

        val notificacao = NotificationCompat.Builder(context, BoletoNotificacoes.CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(context.getString(R.string.notificacao_titulo, nome))
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(abre)
            .build()

        val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
        manager?.notify(id.toInt(), notificacao)
    }
}
