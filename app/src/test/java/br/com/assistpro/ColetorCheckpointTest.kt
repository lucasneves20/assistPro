package br.com.assistpro

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ColetorCheckpointTest {

    @Test
    fun appJsonIncluiDados() {
        val json = JSONObject(ColetorCheckpoint.appJson(true, 3, 4500, "1.2"))
        assertEquals(true, json.getBoolean("modoDev"))
        assertEquals(3, json.getInt("qtdBoletos"))
        assertEquals(4500L, json.getLong("emAbertoCentavos"))
        assertEquals("1.2", json.getString("versaoApp"))
        assertEquals(BuildConfig.APPLICATION_ID, json.getString("pacote"))
    }

    @Test
    fun appJsonModoDesligado() {
        val json = JSONObject(ColetorCheckpoint.appJson(false, 0, 0, "1.0"))
        assertEquals(false, json.getBoolean("modoDev"))
        assertEquals(0, json.getInt("qtdBoletos"))
    }
}
