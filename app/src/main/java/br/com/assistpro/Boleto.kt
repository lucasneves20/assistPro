package br.com.assistpro

data class Boleto(
    var id: Long = 0L,
    var linha: String = "",
    var valorCentavos: Long = 0L,
    var vencimento: String? = null,
    var descricao: String = "",
    var imagem: String? = null,
    var pago: Boolean = false,
    var criadoEm: Long = System.currentTimeMillis()
)
