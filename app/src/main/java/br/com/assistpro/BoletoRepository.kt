package br.com.assistpro

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BoletoDb(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOME, null, VERSAO) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_BOLETOS)
        db.execSQL(SQL_CHECKPOINTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_CHECKPOINTS)
        } else if (oldVersion < 3) {
            db.execSQL(
                "ALTER TABLE $TABELA_CHECKPOINTS ADD COLUMN observacao TEXT NOT NULL DEFAULT ''"
            )
            db.execSQL(
                "ALTER TABLE $TABELA_CHECKPOINTS ADD COLUMN tipo TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    companion object {
        const val NOME = "assistpro.db"
        const val VERSAO = 3
        const val TABELA = "boletos"
        const val TABELA_CHECKPOINTS = "checkpoints"

        val SQL_BOLETOS = """
            CREATE TABLE $TABELA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                linha TEXT NOT NULL DEFAULT '',
                valor INTEGER NOT NULL DEFAULT 0,
                vencimento TEXT,
                descricao TEXT NOT NULL DEFAULT '',
                imagem TEXT,
                pago INTEGER NOT NULL DEFAULT 0,
                criado_em INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        val SQL_CHECKPOINTS = """
            CREATE TABLE IF NOT EXISTS $TABELA_CHECKPOINTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tela TEXT NOT NULL DEFAULT '',
                criado_em INTEGER NOT NULL DEFAULT 0,
                versao_app TEXT NOT NULL DEFAULT '',
                tem_erro INTEGER NOT NULL DEFAULT 0,
                ultimo_erro TEXT,
                app TEXT NOT NULL DEFAULT '',
                sistema TEXT NOT NULL DEFAULT '',
                logs TEXT NOT NULL DEFAULT '',
                observacao TEXT NOT NULL DEFAULT '',
                tipo TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent()
    }
}

class BoletoRepository(context: Context) {

    private val helper = BoletoDb(context)

    fun inserir(b: Boleto): Long {
        val cv = ContentValues().apply {
            put("linha", b.linha)
            put("valor", b.valorCentavos)
            put("vencimento", b.vencimento)
            put("descricao", b.descricao)
            put("imagem", b.imagem)
            put("pago", if (b.pago) 1 else 0)
            put("criado_em", b.criadoEm)
        }
        return helper.writableDatabase.insert(BoletoDb.TABELA, null, cv)
    }

    fun atualizarPago(id: Long, pago: Boolean) {
        val cv = ContentValues().apply { put("pago", if (pago) 1 else 0) }
        helper.writableDatabase.update(BoletoDb.TABELA, cv, "id = ?", arrayOf(id.toString()))
    }

    fun atualizar(b: Boleto) {
        val cv = ContentValues().apply {
            put("linha", b.linha)
            put("valor", b.valorCentavos)
            put("vencimento", b.vencimento)
            put("descricao", b.descricao)
            put("imagem", b.imagem)
            put("pago", if (b.pago) 1 else 0)
        }
        helper.writableDatabase.update(BoletoDb.TABELA, cv, "id = ?", arrayOf(b.id.toString()))
    }

    fun buscar(id: Long): Boleto? {
        helper.readableDatabase.query(
            BoletoDb.TABELA, null, "id = ?", arrayOf(id.toString()), null, null, null
        ).use { c ->
            return if (c.moveToFirst()) fromCursor(c) else null
        }
    }

    fun remover(id: Long) {
        helper.writableDatabase.delete(BoletoDb.TABELA, "id = ?", arrayOf(id.toString()))
    }

    fun listar(): List<Boleto> {
        val out = ArrayList<Boleto>()
        val db = helper.readableDatabase
        db.query(
            BoletoDb.TABELA, null, null, null, null, null,
            "CASE WHEN vencimento IS NULL THEN 1 ELSE 0 END, vencimento ASC, criado_em DESC"
        ).use { c ->
            while (c.moveToNext()) out.add(fromCursor(c))
        }
        return out
    }

    private fun fromCursor(c: Cursor): Boleto = Boleto(
        id = c.getLong(c.getColumnIndexOrThrow("id")),
        linha = c.getString(c.getColumnIndexOrThrow("linha")) ?: "",
        valorCentavos = c.getLong(c.getColumnIndexOrThrow("valor")),
        vencimento = c.getString(c.getColumnIndexOrThrow("vencimento")),
        descricao = c.getString(c.getColumnIndexOrThrow("descricao")) ?: "",
        imagem = c.getString(c.getColumnIndexOrThrow("imagem")),
        pago = c.getInt(c.getColumnIndexOrThrow("pago")) == 1,
        criadoEm = c.getLong(c.getColumnIndexOrThrow("criado_em"))
    )
}
