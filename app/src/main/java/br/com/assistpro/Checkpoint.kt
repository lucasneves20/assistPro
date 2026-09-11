package br.com.assistpro

/**
 * Estado capturado pelo modo desenvolvedor em uma tela.
 * Guardado na tabela "checkpoints" do banco assistpro.db.
 */
data class Checkpoint(
    var id: Long = 0L,
    var tela: String = "",
    var criadoEm: Long = System.currentTimeMillis(),
    var versaoApp: String = "",
    var temErro: Boolean = false,
    var ultimoErro: String? = null,
    var app: String = "",
    var sistema: String = "",
    var logs: String = ""
)
