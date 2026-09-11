package br.com.assistpro

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifica que o calendario realmente renderiza: os dias da semana aparecem e
 * as celulas dos dias tem largura/altura maiores que zero apos o layout.
 * Isso cobre o bug relatado de "nao aparecer os dias".
 *
 * Observacao: o Robolectric nao roda no Termux (a lib nativa do Conscrypt nao
 * tem build para Android/aarch64). Nesse ambiente o teste e ignorado; em
 * desktop/CI ele executa normalmente. O calculo dos dias tambem e coberto
 * pelo CalendarioMesTest (JVM puro).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CalendarioMesViewTest {

    @Before
    fun somenteForaDoTermux() {
        val prefixo = System.getenv("PREFIX") ?: ""
        Assume.assumeFalse(
            "Robolectric indisponivel no Termux",
            prefixo.contains("com.termux")
        )
    }

    private fun textViews(view: View): List<TextView> {
        val out = ArrayList<TextView>()
        fun andar(v: View) {
            if (v is TextView) out.add(v)
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) andar(v.getChildAt(i))
            }
        }
        andar(view)
        return out
    }

    private fun renderizar(ano: Int, mes: Int): List<TextView> {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        val view = CalendarioMesView(contexto)
        view.definirMes(ano, mes)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        return textViews(view)
    }

    @Test
    fun renderizaDiasDaSemanaEDiasDoMes() {
        val textos = renderizar(2024, 1)
        val conteudo = textos.map { it.text.toString() }

        assertTrue(
            "dias da semana ausentes",
            conteudo.containsAll(listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab"))
        )
        assertTrue("dia 1 ausente", conteudo.contains("1"))
        assertTrue("dia 31 ausente", conteudo.contains("31"))
        assertFalse("dia 32 nao deveria existir", conteudo.contains("32"))
    }

    @Test
    fun celulasTemTamanhoDeRenderizacao() {
        val textos = renderizar(2024, 1)

        val celula1 = textos.first { it.text.toString() == "1" }
        assertTrue("celula do dia 1 sem largura", celula1.measuredWidth > 0)
        assertTrue("celula do dia 1 sem altura", celula1.measuredHeight > 0)

        val rotulo = textos.first { it.text.toString() == "Seg" }
        assertTrue("rotulo de dia da semana sem largura", rotulo.measuredWidth > 0)
    }

    @Test
    fun fevereiroBissextoTem29() {
        val conteudo = renderizar(2024, 2).map { it.text.toString() }
        assertTrue("dia 29 ausente em fevereiro bissexto", conteudo.contains("29"))
        assertFalse("dia 30 nao deveria existir em fevereiro", conteudo.contains("30"))
    }
}
