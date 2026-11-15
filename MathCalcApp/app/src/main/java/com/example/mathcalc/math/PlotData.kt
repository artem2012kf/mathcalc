package com.example.mathcalc.math

object PlotData {

    /** Jump threshold above which a point is treated as a discontinuity. */
    private const val JUMP_LIMIT = 1e6

    /**
     * Samples an expression over [xMin, xMax].
     * Returns (x, y?) pairs; y is null when the value is undefined (NaN/Infinity)
     * or a discontinuity is detected (|y| jump > 1e6 compared to the previous finite point).
     */
    fun sample(
        expr: String,
        xMin: Double,
        xMax: Double,
        points: Int = 400,
    ): List<Pair<Double, Double?>> {
        require(points >= 2) { "Нужно минимум 2 точки для построения графика" }
        val result = ArrayList<Pair<Double, Double?>>(points + 1)
        var lastFiniteY: Double? = null
        var lastX: Double? = null

        for (i in 0 until points) {
            val x = xMin + (xMax - xMin) * i / (points - 1.0)
            val y = try {
                val v = Calculator.evaluate(expr, x)
                if (v.isNaN() || v.isInfinite()) null else v
            } catch (e: IllegalArgumentException) {
                null
            }

            val visible = when {
                y == null -> false
                lastFiniteY != null && lastX != null && kotlin.math.abs(y - lastFiniteY) > JUMP_LIMIT &&
                    kotlin.math.abs(x - lastX) < (xMax - xMin) / 10 -> false
                else -> true
            }

            result += x to if (visible) y else null

            if (y != null && visible) {
                lastFiniteY = y
                lastX = x
            }
        }
        return result
    }

    /**
     * Finds zeros of an expression on [xMin, xMax]: detects sign changes between
     * consecutive samples and refines each root by linear interpolation.
     */
    fun zeros(
        expr: String,
        xMin: Double,
        xMax: Double,
        points: Int = 1000,
    ): List<Double> {
        require(points >= 2) { "Нужно минимум 2 точки для поиска корней" }
        val roots = mutableListOf<Double>()

        fun f(x: Double): Double? = try {
            Calculator.evaluate(expr, x).takeIf { it.isFinite() }
        } catch (e: IllegalArgumentException) {
            null
        }

        var prevX = xMin
        var prevY = f(prevX)
        for (i in 1..points) {
            val x = xMin + (xMax - xMin) * i / (points - 1.0)
            val y = f(x)
            if (prevY != null && y != null) {
                if (prevY == 0.0) {
                    addUnique(roots, prevX)
                } else if (prevY * y < 0) {
                    // Linear interpolation refinement, then Newton steps for precision.
                    var root = prevX - prevY * (x - prevX) / (y - prevY)
                    repeat(4) {
                        val fy = f(root) ?: return@repeat
                        val df = ((f(root + 1e-7) ?: fy) - fy) / 1e-7
                        if (df != 0.0) root -= fy / df
                    }
                    addUnique(roots, root)
                }
            }
            prevX = x
            prevY = y
        }
        if (prevY == 0.0) addUnique(roots, prevX)
        return roots
    }

    private fun addUnique(list: MutableList<Double>, value: Double) {
        if (list.none { kotlin.math.abs(it - value) < 1e-6 }) list += value
    }
}
