package br.com.assistpro

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class GanhoRepository(context: Context) {

    private val helper = BoletoDb(context.applicationContext)

    fun inserir(g: Ganho): Long {
        val cv = ContentValues().apply {
            put("descricao", g.descricao)
            put("valor", g.valorCentavos)
            put("data", g.data)
            put("criado_em", g.criadoEm)
        }
        return helper.writableDatabase.insert(BoletoDb.TABELA_GANHOS, null, cv)
    }

    fun remover(id: Long) {
        helper.writableDatabase.delete(BoletoDb.TABELA_GANHOS, "id = ?", arrayOf(id.toString()))
    }

    fun listar(): List<Ganho> {
        val out = ArrayList<Ganho>()
        helper.readableDatabase.query(
            BoletoDb.TABELA_GANHOS, null, null, null, null, null, "data DESC, criado_em DESC"
        ).use { c ->
            while (c.moveToNext()) out.add(fromCursor(c))
        }
        return out
    }

    private fun fromCursor(c: Cursor): Ganho = Ganho(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        descricao = c.getString(c.getColumnIndexOrThrow("descricao")) ?: "",
        valorCentavos = c.getLong(c.getColumnIndexOrThrow("valor")),
        data = c.getString(c.getColumnIndexOrThrow("data")) ?: "",
        criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
    )
}
