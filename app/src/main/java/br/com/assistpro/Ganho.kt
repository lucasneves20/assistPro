package br.com.assistpro

/**
 * Lancamento de ganho (receita). As perdas continuam sendo os boletos.
 */
data class Ganho(
    var id: Long = 0L,
    var descricao: String = "",
    var valorCentavos: Long = 0L,
    var data: String = "",
    var criadoEm: Long = System.currentTimeMillis()
)
