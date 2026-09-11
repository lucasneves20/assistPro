package br.com.assistpro

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Environment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class CheckpointRepository(context: Context) {

    private val appContext = context.applicationContext
    private val helper = BoletoDb(appContext)

    fun inserir(c: Checkpoint): Long {
        val cv = ContentValues().apply {
            put("tela", c.tela)
            put("criado_em", c.criadoEm)
            put("versao_app", c.versaoApp)
            put("tem_erro", if (c.temErro) 1 else 0)
            put("ultimo_erro", c.ultimoErro)
            put("app", c.app)
            put("sistema", c.sistema)
            put("logs", c.logs)
        }
        return helper.writableDatabase.insert(BoletoDb.TABELA_CHECKPOINTS, null, cv)
    }

    fun listar(): List<Checkpoint> {
        val out = ArrayList<Checkpoint>()
        helper.readableDatabase.query(
            BoletoDb.TABELA_CHECKPOINTS, null, null, null, null, null, "criado_em DESC"
        ).use { c ->
            while (c.moveToNext()) out.add(fromCursor(c))
        }
        return out
    }

    fun contar(): Int {
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM ${BoletoDb.TABELA_CHECKPOINTS}", null
        ).use { c ->
            return if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    fun limpar() {
        helper.writableDatabase.delete(BoletoDb.TABELA_CHECKPOINTS, null, null)
    }

    private fun fromCursor(c: Cursor): Checkpoint = Checkpoint(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        tela = c.getString(c.getColumnIndexOrThrow("tela")) ?: "",
        criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em")),
        versaoApp = c.getString(c.getColumnIndexOrThrow("versao_app")) ?: "",
        temErro = c.getInt(c.getColumnIndexOrThrow("tem_erro")) == 1,
        ultimoErro = c.getString(c.getColumnIndexOrThrow("ultimo_erro")),
        app = c.getString(c.getColumnIndexOrThrow("app")) ?: "",
        sistema = c.getString(c.getColumnIndexOrThrow("sistema")) ?: "",
        logs = c.getString(c.getColumnIndexOrThrow("logs")) ?: ""
    )

    companion object {
        /**
         * Copia todos os checkpoints para Download/assistpro_checkpoints.json,
         * de onde podem ser lidos fora do app (ex.: pelo agente em desenvolvimento).
         * Retorna o arquivo ou null se nao foi possivel escrever.
         */
        fun exportar(context: Context): File? {
            val lista = CheckpointRepository(context).listar()
            val arr = JSONArray()
            for (c in lista) {
                arr.put(
                    JSONObject().apply {
                        put("id", c.id)
                        put("tela", c.tela)
                        put("criadoEm", c.criadoEm)
                        put("versaoApp", c.versaoApp)
                        put("temErro", c.temErro)
                        put("ultimoErro", c.ultimoErro ?: JSONObject.NULL)
                        put("app", jsonOuTexto(c.app))
                        put("sistema", jsonOuTexto(c.sistema))
                        put("logs", c.logs)
                    }
                )
            }
            return try {
                val dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                if (!dir.exists()) dir.mkdirs()
                val arquivo = File(dir, "assistpro_checkpoints.json")
                arquivo.writeText(arr.toString(2))
                arquivo
            } catch (e: Exception) {
                LogApp.e("Falha ao exportar checkpoints", e)
                null
            }
        }

        private fun jsonOuTexto(bruto: String): Any =
            try {
                if (bruto.isBlank()) JSONObject.NULL else JSONObject(bruto)
            } catch (e: Exception) {
                bruto
            }
    }
}
