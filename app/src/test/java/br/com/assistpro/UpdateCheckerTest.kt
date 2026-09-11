package br.com.assistpro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun detectaVersaoMaisNova() {
        assertTrue(UpdateChecker.maisNova("1.1", "1.0"))
        assertTrue(UpdateChecker.maisNova("2", "1.9"))
        assertTrue(UpdateChecker.maisNova("1.0.1", "1.0"))
        assertTrue(UpdateChecker.maisNova("1.10", "1.9"))
    }

    @Test
    fun ignoraVersaoIgualOuAntiga() {
        assertFalse(UpdateChecker.maisNova("1.0", "1.0"))
        assertFalse(UpdateChecker.maisNova("1.0", "1.1"))
        assertFalse(UpdateChecker.maisNova("1.0.0", "1.0"))
    }
}
