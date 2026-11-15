package com.example.mathcalc

import com.example.mathcalc.math.Calculator
import com.example.mathcalc.math.EquationSolver
import com.example.mathcalc.math.PlotData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MathTest {

    private fun assertApprox(expected: Double, actual: Double, eps: Double = 1e-9) {
        assertEquals(expected, actual, eps)
    }

    // ---------- Calculator ----------

    @Test
    fun `operator precedence 2 plus 2 times 2 equals 6`() {
        assertApprox(6.0, Calculator.evaluate("2+2*2"))
    }

    @Test
    fun `sqrt 9 equals 3`() {
        assertApprox(3.0, Calculator.evaluate("sqrt(9)"))
        assertApprox(3.0, Calculator.evaluate("√9"))
        assertApprox(3.0, Calculator.evaluate("√(9)"))
    }

    @Test
    fun `parenthesized power (2 plus 3) squared equals 25`() {
        assertApprox(25.0, Calculator.evaluate("(2+3)^2"))
    }

    @Test
    fun `unary minus and division`() {
        assertApprox(-3.0, Calculator.evaluate("-6/2"))
        assertApprox(-8.0, Calculator.evaluate("-2^3")) // unary minus binds looser than power
    }

    @Test
    fun `constants and variable`() {
        assertApprox(Math.PI, Calculator.evaluate("pi"))
        assertApprox(Math.E * 2, Calculator.evaluate("e*2"))
        assertApprox(10.0, Calculator.evaluate("2*x+2", 4.0))
        assertApprox(1.0, Calculator.evaluate("x^0", 123.0))
    }

    @Test
    fun `whitespace is skipped`() {
        assertApprox(7.0, Calculator.evaluate(" 3 + 4 "))
    }

    @Test
    fun `invalid input throws with message`() {
        try {
            Calculator.evaluate("2+*3")
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotBlank())
        }
        try {
            Calculator.evaluate("sqrt(-4)")
            throw AssertionError("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("корень"))
        }
    }

    @Test
    fun `formatNumber trims trailing zeros`() {
        assertEquals("2.5", Calculator.formatNumber(2.500000))
        assertEquals("3", Calculator.formatNumber(3.0))
        assertEquals("-0.1", Calculator.formatNumber(-0.1))
        assertEquals("0.33333", Calculator.formatNumber(0.33333))
    }

    // ---------- EquationSolver ----------

    @Test
    fun `quadratic with two roots x2 minus 5x plus 6`() {
        val r = EquationSolver.solveQuadratic(1.0, -5.0, 6.0)
        assertEquals(2, r.roots.size)
        assertApprox(3.0, r.roots[0])
        assertApprox(2.0, r.roots[1])
        assertTrue(r.steps.isNotEmpty())
        assertTrue(r.summary.contains("3"))
    }

    @Test
    fun `quadratic with negative discriminant has no real roots`() {
        val r = EquationSolver.solveQuadratic(1.0, 0.0, 1.0)
        assertTrue(r.roots.isEmpty())
        assertTrue(r.steps.any { it.detail.contains("< 0") })
    }

    @Test
    fun `quadratic with zero discriminant single root`() {
        val r = EquationSolver.solveQuadratic(1.0, -2.0, 1.0)
        assertEquals(listOf(1.0), r.roots.map { it })
    }

    @Test
    fun `quadratic discriminant simplification shows exact form`() {
        // 2x² - 5x - 3: D = 25 + 24 = 49, perfect square → exact decimal roots
        val r = EquationSolver.solveQuadratic(2.0, -5.0, -3.0)
        assertEquals(2, r.roots.size)
        assertApprox(3.0, r.roots[0])
        assertApprox(-0.5, r.roots[1])
    }

    @Test
    fun `linear equation`() {
        val r = EquationSolver.solveLinear(2.0, -6.0)
        assertEquals(1, r.roots.size)
        assertApprox(3.0, r.roots[0])
        assertTrue(r.steps.any { it.title.contains("Переносим") })
        assertTrue(r.steps.any { it.title.contains("Делим") })
    }

    @Test
    fun `linear with zero a`() {
        val r = EquationSolver.solveLinear(0.0, 5.0)
        assertTrue(r.roots.isEmpty())
        val identity = EquationSolver.solveLinear(0.0, 0.0)
        assertTrue(identity.roots.isEmpty())
        assertTrue(identity.summary.contains("бесконечно"))
    }

    @Test
    fun `cubic x3 minus 6x2 plus 11x minus 6 roots 1 2 3`() {
        val r = EquationSolver.solveCubic(1.0, -6.0, 11.0, -6.0)
        assertEquals(3, r.roots.size)
        assertApprox(1.0, r.roots[0])
        assertApprox(2.0, r.roots[1])
        assertApprox(3.0, r.roots[2])
        assertTrue(r.steps.isNotEmpty())
    }

    @Test
    fun `cubic without rational root uses numeric method`() {
        // x³ - 2x - 5 (classic example, one real root ≈ 2.0946)
        val r = EquationSolver.solveCubic(1.0, 0.0, -2.0, -5.0)
        assertTrue(r.roots.isNotEmpty())
        assertApprox(2.0946, r.roots.last(), 1e-3)
    }

    @Test
    fun `solve dispatcher`() {
        val r = EquationSolver.solve("quadratic", listOf(1.0, -5.0, 6.0))
        assertEquals(2, r.roots.size)
        val lin = EquationSolver.solve("linear", listOf(4.0, -8.0))
        assertApprox(2.0, lin.roots[0])
        val cub = EquationSolver.solve("cubic", listOf(1.0, -6.0, 11.0, -6.0))
        assertEquals(3, cub.roots.size)
    }

    // ---------- PlotData ----------

    @Test
    fun `sample of straight line`() {
        val points = PlotData.sample("2*x+1", -5.0, 5.0, 101)
        assertEquals(101, points.size)
        assertTrue(points.all { it.second != null })
        val first = points.first()
        assertEquals(-5.0, first.first, 1e-9)
        assertApprox(-9.0, first.second!!, 1e-9)
    }

    @Test
    fun `sample marks undefined points as null`() {
        val points = PlotData.sample("1/x", -2.0, 2.0, 401)
        val nulls = points.count { it.second == null }
        assertTrue("Должна быть точка разрыва возле x=0", nulls > 0)
    }

    @Test
    fun `zeros of parabola`() {
        val roots = PlotData.zeros("x^2-5*x+6", -10.0, 10.0)
        assertEquals(2, roots.size)
        assertApprox(2.0, roots[0], 1e-4)
        assertApprox(3.0, roots[1], 1e-4)
    }

    @Test
    fun `zeros of sine on wide range`() {
        val roots = PlotData.zeros("sin(x)", -10.0, 10.0, 2000)
        assertTrue(roots.size >= 6)
    }
}
