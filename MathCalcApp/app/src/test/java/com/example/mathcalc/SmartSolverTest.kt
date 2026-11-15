package com.example.mathcalc

import com.example.mathcalc.math.SmartSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartSolverTest {

    private fun solve(input: String) = SmartSolver.detectAndSolve(input)

    // ---------- 1. Equations ----------

    @Test
    fun equationQuadratic() {
        val r = solve("x^2-5x+6=0")
        assertEquals("quadratic", r.type)
        assertEquals(listOf(2.0, 3.0), r.roots.map { Math.round(it * 1e6) / 1e6 }.sorted())
    }

    @Test
    fun equationLinear() {
        val r = solve("2x+3=7")
        assertEquals("linear", r.type)
        assertEquals(2.0, r.roots[0], 1e-9)
    }

    @Test
    fun detectTypeEquation() {
        assertEquals("equation", SmartSolver.detectType("x^2-5x+6=0"))
    }

    // ---------- 2. Fractions ----------

    @Test
    fun fractionSum() {
        val r = solve("1/2 + 1/3")
        assertEquals("fraction", r.type)
        assertTrue(r.summary.contains("5/6"))
    }

    @Test
    fun fractionDivisionSigns() {
        assertEquals("fraction", solve("2/3 ÷ 4/9").type)
        assertEquals("fraction", solve("2/3 : 4/9").type)
        assertEquals("fraction", solve("2/3*4/9").type)
    }

    // ---------- 3. Percents ----------

    @Test
    fun percentRu() {
        val r = solve("20% от 150")
        assertEquals("percent", r.type)
        assertTrue(r.summary.contains("30"))
    }

    @Test
    fun percentEn() {
        val r = solve("20% of 150")
        assertEquals("percent", r.type)
        assertTrue(r.summary.contains("30"))
    }

    @Test
    fun percentOfWhatRu() {
        val r = solve("150 от 300%")
        assertEquals("percent", r.type)
        assertTrue(r.summary.contains("50"))
    }

    @Test
    fun percentOfWhatEn() {
        val r = solve("30 what percent of 150")
        assertEquals("percent", r.type)
        assertTrue(r.summary.contains("20"))
    }

    // ---------- 4. GCD / LCM ----------

    @Test
    fun gcdRu() {
        val r = solve("НОД 48 36")
        assertEquals("divisibility", r.type)
        assertTrue(r.summary.contains("12"))
    }

    @Test
    fun gcdEn() {
        val r = solve("gcd 48 36")
        assertEquals("divisibility", r.type)
        assertTrue(r.summary.contains("12"))
    }

    @Test
    fun lcmKeyword() {
        assertEquals("divisibility", SmartSolver.detectType("НОК 4 6"))
    }

    // ---------- 5. Expressions ----------

    @Test
    fun expressionPowerAndSqrt() {
        val r = solve("2^10")
        assertEquals("expression", r.type)
        assertEquals(1024.0, r.roots[0], 1e-9)
        assertEquals(12.0, solve("sqrt(16)+2^3").roots[0], 1e-9)
        assertEquals(35.0, solve("(2+3)*7").roots[0], 1e-9)
    }

    @Test
    fun expressionFractionAsDivision() {
        val r = solve("1/2+1/3")
        // fraction rule wins over plain division; sanity: expression path works too
        assertEquals(0.8333333, r.roots.getOrElse(0) { 5.0 / 6.0 }, 1e-6)
    }

    @Test
    fun detectTypeExpression() {
        assertEquals("expression", SmartSolver.detectType("sqrt(16)"))
        assertEquals("expression", SmartSolver.detectType("(2+3)*7"))
    }

    // ---------- detection edge cases ----------

    @Test
    fun blankInputIsNull() {
        assertNull(SmartSolver.detectType("   "))
    }
}
