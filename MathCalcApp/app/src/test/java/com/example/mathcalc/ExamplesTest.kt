package com.example.mathcalc

import com.example.mathcalc.math.EquationSolver
import com.example.mathcalc.math.Examples
import com.example.mathcalc.math.Fraction
import com.example.mathcalc.math.FractionSolver
import com.example.mathcalc.math.L10n
import com.example.mathcalc.math.PhotoEquationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExamplesTest {

    @Before
    fun setUp() {
        L10n.lang = "ru"
    }

    private fun assertApprox(expected: Double, actual: Double, eps: Double = 1e-9) {
        assertEquals(expected, actual, eps)
    }

    // ---------- Fraction ----------

    @Test
    fun `fraction normalizes 3 over 9 to 1 over 3`() {
        assertEquals(Fraction(1, 3), Fraction(3, 9))
        assertEquals("1/3", Fraction(3, 9).toString())
    }

    @Test
    fun `fraction normalizes negative denominator`() {
        assertEquals(Fraction(-1, 3), Fraction(1, -3))
        assertEquals("1/3", Fraction(-1, -3).toString())
    }

    @Test
    fun `one half plus one third equals five sixths`() {
        val sum = Fraction(1, 2) + Fraction(1, 3)
        assertEquals(Fraction(5, 6), sum)
        assertEquals("5/6", sum.toString())
    }

    @Test
    fun `fraction subtraction and multiplication`() {
        assertEquals(Fraction(1, 6), Fraction(1, 2) - Fraction(1, 3))
        assertEquals(Fraction(1, 6), Fraction(1, 2) * Fraction(1, 3))
    }

    @Test
    fun `fraction division by reciprocal`() {
        assertEquals(Fraction(3, 2), Fraction(1, 2) / Fraction(1, 3))
    }

    @Test
    fun `fraction toString integer and mixed`() {
        assertEquals("2", Fraction(4, 2).toString())
        assertEquals("1 1/2", Fraction(3, 2).toString())
        assertEquals("-1 1/2", Fraction(-3, 2).toString())
    }

    @Test
    fun `fraction toDecimal`() {
        assertApprox(0.75, Fraction(3, 4).toDecimal())
        assertApprox(1.5, Fraction(3, 2).toDecimal())
    }

    @Test
    fun `fraction equality is independent of representation`() {
        assertEquals(Fraction(1, 3), Fraction(2, 6))
        assertTrue(Fraction(1, 3) == Fraction(3, 9))
        assertFalse(Fraction(1, 3) == Fraction(1, 2))
    }

    @Test
    fun `solveFractionExample addition steps mention common denominator`() {
        val res = FractionSolver.solveFractionExample(Fraction(1, 2), Fraction(1, 3), '+')
        assertTrue(res.summary.contains("5/6"))
        assertTrue(res.steps.any { it.title == L10n.t("fracCommonDenominator") })
        assertTrue(res.steps.any { it.detail.contains("НОК(2, 3) = 6") })
    }

    @Test
    fun `solveFractionExample division inverts second fraction`() {
        val res = FractionSolver.solveFractionExample(Fraction(1, 2), Fraction(1, 3), '/')
        assertTrue(res.summary.contains("1 1/2"))
        assertTrue(res.steps.any { it.title == L10n.t("fracInvert") })
        assertTrue(res.steps.any { it.detail.contains("3/2") })
    }

    @Test
    fun `solveFractionExample multiplication shows cross multiply`() {
        val res = FractionSolver.solveFractionExample(Fraction(1, 2), Fraction(1, 3), '*')
        assertTrue(res.summary.contains("1/6"))
        assertTrue(res.steps.any { it.title == L10n.t("fracCrossMultiply") })
    }

    // ---------- Percents ----------

    @Test
    fun `percentageOf 15 percent of 250 equals 37 dot 5`() {
        val res = Examples.percentageOf(250.0, 15.0)
        assertTrue(res.summary.contains("37.5"))
    }

    @Test
    fun `percentOfWhat 50 of 200 is 25 percent`() {
        val res = Examples.percentOfWhat(50.0, 200.0)
        assertTrue(res.summary.contains("25"))
    }

    // ---------- Divisibility ----------

    @Test
    fun `gcd and lcm of 12 and 8`() {
        val res = Examples.gcdLcm(12, 8)
        assertTrue(res.summary.contains("НОД(12, 8) = 4"))
        assertTrue(res.summary.contains("НОК(12, 8) = 24"))
    }

    @Test
    fun `euclid steps present`() {
        val res = Examples.gcdLcm(48, 18)
        assertTrue(res.steps.any { L10n.t("euclidDetail", 48, 18, 2, 12) == it.detail })
    }

    @Test
    fun `prime factorization of 360`() {
        val res = Examples.primeFactorization(360)
        assertTrue(res.summary.contains("2³ · 3² · 5"))
    }

    @Test
    fun `prime factorization of prime 13`() {
        val res = Examples.primeFactorization(13)
        assertTrue(res.summary.contains("13 = 13"))
    }

    @Test
    fun `powerOf 2 to 10 equals 1024`() {
        val res = Examples.powerOf(2, 10)
        assertTrue(res.summary.contains("2^10 = 1024"))
    }

    @Test
    fun `powerOf zero exponent equals 1`() {
        val res = Examples.powerOf(7, 0)
        assertTrue(res.summary.contains("7^0 = 1"))
    }

    // ---------- EquationSolver.solvePolynomial ----------

    @Test
    fun `solvePolynomial dispatches linear`() {
        val res = EquationSolver.solvePolynomial(listOf(2.0, -4.0))
        assertEquals("linear", res.type)
        assertApprox(2.0, res.roots.first())
    }

    @Test
    fun `solvePolynomial dispatches quadratic`() {
        val res = EquationSolver.solvePolynomial(listOf(1.0, -5.0, 6.0))
        assertEquals("quadratic", res.type)
        assertEquals(listOf(3.0, 2.0), res.roots)
    }

    // ---------- PhotoEquationParser ----------

    private fun coefs(vararg v: Double) = v.toList()

    @Test
    fun `parse quadratic with caret`() {
        val (degree, c, cleaned) = PhotoEquationParser.parseEquation("x^2-5x+6=0")!!
        assertEquals(2, degree)
        assertEquals(coefs(1.0, -5.0, 6.0), c)
        assertEquals("x^2-5x+6=0", cleaned)
    }

    @Test
    fun `parse linear standard form 2x plus 3 equals 7`() {
        val (degree, c, _) = PhotoEquationParser.parseEquation("2x+3=7")!!
        assertEquals(1, degree)
        assertEquals(coefs(2.0, -4.0), c)
    }

    @Test
    fun `parse superscripts and unicode minus`() {
        val (degree, c, _) = PhotoEquationParser.parseEquation("x²−5x+6=0")!!
        assertEquals(2, degree)
        assertEquals(coefs(1.0, -5.0, 6.0), c)
    }

    @Test
    fun `parse cyrillic x and comma decimal`() {
        val (degree, c, cleaned) = PhotoEquationParser.parseEquation("1,5х+3=0")!!
        assertEquals(1, degree)
        assertEquals(coefs(1.5, 3.0), c)
        assertEquals("1.5x+3=0", cleaned)
    }

    @Test
    fun `parse removes spaces and multiplication signs`() {
        val (degree, c, _) = PhotoEquationParser.parseEquation(" 2 × x + 3 = 7 ")!!
        assertEquals(1, degree)
        assertEquals(coefs(2.0, -4.0), c)
    }

    @Test
    fun `parse cubic with superscript`() {
        val (degree, c, _) = PhotoEquationParser.parseEquation("x³-8=0")!!
        assertEquals(3, degree)
        assertEquals(coefs(1.0, 0.0, 0.0, -8.0), c)
    }

    @Test
    fun `parse equation with variable on both sides`() {
        val (degree, c, _) = PhotoEquationParser.parseEquation("3x+2=x+10")!!
        assertEquals(1, degree)
        assertEquals(coefs(2.0, -8.0), c)
    }

    @Test
    fun `parse returns null for garbage`() {
        assertNull(PhotoEquationParser.parseEquation("hello world"))
        assertNull(PhotoEquationParser.parseEquation("2+2*2"))
        assertNull(PhotoEquationParser.parseEquation("x^5-1=0"))
    }
}
