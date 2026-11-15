package com.example.mathcalc.math

/**
 * Smart auto-detection for the free-text input: figures out what the user typed
 * (equation, fraction arithmetic, percent problem, GCD/LCM, or a plain expression)
 * and dispatches to the matching solver.
 *
 * Detection order:
 *  1. contains '=' and a variable x  -> PhotoEquationParser + EquationSolver.solvePolynomial
 *  2. "a/b op c/d" fraction pattern  -> FractionSolver
 *  3. percent phrases (ru and en)    -> Examples.percentageOf / percentOfWhat
 *  4. НОД/gcd/НОК/lcm keywords       -> Examples.gcdLcm
 *  5. otherwise                      -> Calculator.evaluate (expression)
 */
object SmartSolver {

    // ---------- public API ----------

    /** Detects the problem type without solving: "equation", "fraction", "percent",
     *  "divisibility" or "expression"; null when the input is blank. */
    fun detectType(input: String): String? {
        val t = input.trim()
        if (t.isBlank()) return null
        if (isEquation(t)) return "equation"
        if (FRACTION_RE.matches(t)) return "fraction"
        val low = t.lowercase()
        if (matchPercentOf(low) != null || matchPercentOfWhat(low) != null) return "percent"
        if (GCD_RE.containsMatchIn(low)) return "divisibility"
        return "expression"
    }

    /** Detects and solves the input, throwing IllegalArgumentException for bad input. */
    fun detectAndSolve(input: String): EquationResult {
        val t = input.trim()
        if (t.isBlank()) throw IllegalArgumentException(L10n.t("emptyExpr"))

        // 1. Polynomial equation in x: "x^2-5x+6=0", "2х+3=7", "x²−5x+6=0"
        if (isEquation(t)) {
            val parsed = PhotoEquationParser.parseEquation(t)
                ?: throw IllegalArgumentException(L10n.t("photoError"))
            return EquationSolver.solvePolynomial(parsed.second)
        }

        // 2. Fraction arithmetic: "1/2 + 1/3", "2/3 ÷ 4/9"
        FRACTION_RE.find(t)?.let { m ->
            val (a, b, op, c, d) = m.destructured
            return FractionSolver.solveFractionExample(
                Fraction(a.toLong(), b.toLong()),
                Fraction(c.toLong(), d.toLong()),
                mapOp(op)
            )
        }

        val low = t.lowercase()

        // 3. Percent: "20% от 150" / "20% of 150" / "150 is what percent of 300" / "150 от 300%"
        matchPercentOf(low)?.let { (p, x) -> return Examples.percentageOf(x, p) }
        matchPercentOfWhat(low)?.let { (x, y) -> return Examples.percentOfWhat(x, y) }

        // 4. Divisibility: "НОД 48 36" / "gcd 48 36" / "НОК 4 6" / "lcm 4 6"
        if (GCD_RE.containsMatchIn(low)) {
            val nums = NUM_RE.findAll(t).map { it.value.toLong() }.toList()
            if (nums.size >= 2) return Examples.gcdLcm(nums[0], nums[1])
        }

        // 5. Plain expression: "(2+3)*7", "2^10", "sqrt(16)", "1/2+1/3" (as division)
        val expr = PhotoEquationParser.normalize(t)
        val value = Calculator.evaluate(expr)
        val formatted = Calculator.formatNumber(value)
        val summary = "$expr = $formatted"
        return EquationResult(
            type = "expression",
            steps = listOf(
                SolutionStep(L10n.t("fracOriginal"), expr),
                SolutionStep(L10n.t("result"), formatted)
            ),
            roots = listOf(value),
            summary = summary
        )
    }

    // ---------- detection helpers ----------

    private fun isEquation(t: String): Boolean =
        t.contains('=') && PhotoEquationParser.normalize(t).contains('x')

    /** "a/b op c/d" with integer operands. */
    private val FRACTION_RE = Regex(
        """^\s*(-?\d+)\s*/\s*(\d+)\s*([+\-*/:÷·×])\s*(-?\d+)\s*/\s*(\d+)\s*$"""
    )

    private fun mapOp(op: String): Char = when (op) {
        "÷", ":", "/" -> '/'
        "·", "×", "*" -> '*'
        else -> op.first()
    }

    private val NUM_RE = Regex("""\d+(?:[.,]\d+)?""")

    private val GCD_RE = Regex("""(нод|gcd|нок|lcm)""")

    private val PERCENT_OF_RE = Regex(
        """(\d+(?:[.,]\d+)?)\s*%\s*(?:от|of)\s*(\d+(?:[.,]\d+)?)"""
    )

    private val PERCENT_OF_WHAT_RE = Regex(
        """(\d+(?:[.,]\d+)?)\s*(?:от|of)\s*(\d+(?:[.,]\d+)?)\s*%"""
    )

    private val WHAT_PERCENT_RE = Regex("""(what percent|какой процент|какой процент от|какой %)""")

    private fun toD(s: String): Double = s.replace(',', '.').toDouble()

    /** "N% от X" / "N% of X" -> (p, x). */
    private fun matchPercentOf(low: String): Pair<Double, Double>? =
        PERCENT_OF_RE.find(low)?.let { toD(it.groupValues[1]) to toD(it.groupValues[2]) }

    /** "X от Y%" / "X is what percent of Y" / "X какой процент от Y" -> (x, y). */
    private fun matchPercentOfWhat(low: String): Pair<Double, Double>? {
        PERCENT_OF_WHAT_RE.find(low)?.let { return toD(it.groupValues[1]) to toD(it.groupValues[2]) }
        if (WHAT_PERCENT_RE.containsMatchIn(low)) {
            val nums = NUM_RE.findAll(low).map { toD(it.value) }.toList()
            if (nums.size >= 2) return nums[0] to nums[1]
        }
        return null
    }
}
