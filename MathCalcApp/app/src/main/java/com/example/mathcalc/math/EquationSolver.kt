package com.example.mathcalc.math

data class SolutionStep(val title: String, val detail: String)

data class EquationResult(
    val type: String,
    val steps: List<SolutionStep>,
    val roots: List<Double>,
    val summary: String,
)

object EquationSolver {

    /** Dispatches on the coefficient count: [a,b] linear, [a,b,c] quadratic, [a,b,c,d] cubic. */
    fun solvePolynomial(coefs: List<Double>): EquationResult = when (coefs.size) {
        2 -> solveLinear(coefs[0], coefs[1])
        3 -> solveQuadratic(coefs[0], coefs[1], coefs[2])
        4 -> solveCubic(coefs[0], coefs[1], coefs[2], coefs[3])
        else -> throw IllegalArgumentException(L10n.t("needCoefs", "?", coefs.size))
    }

    fun solve(type: String, coefs: List<Double>): EquationResult = when (type.lowercase()) {
        "linear" -> {
            require(coefs.size >= 2) { L10n.t("needCoefs", L10n.t("eqLinear"), 2) }
            solveLinear(coefs[0], coefs[1])
        }
        "quadratic" -> {
            require(coefs.size >= 3) { L10n.t("needCoefs", L10n.t("eqQuadratic"), 3) }
            solveQuadratic(coefs[0], coefs[1], coefs[2])
        }
        "cubic" -> {
            require(coefs.size >= 4) { L10n.t("needCoefs", L10n.t("eqCubic"), 4) }
            solveCubic(coefs[0], coefs[1], coefs[2], coefs[3])
        }
        else -> throw IllegalArgumentException(L10n.t("unknownType", type))
    }

    // ---------- Linear ----------

    fun solveLinear(a: Double, b: Double): EquationResult {
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep(L10n.t("originalEquation"), "${fmt(a)}x + ${fmt(b)} = 0")
        if (a == 0.0) {
            return if (b == 0.0) {
                steps += SolutionStep(L10n.t("analysis"), L10n.t("linearIdentityDetail"))
                EquationResult("linear", steps, emptyList(), L10n.t("identitySummary"))
            } else {
                steps += SolutionStep(L10n.t("analysis"), L10n.t("linearNoRootsDetail", fmt(b)))
                EquationResult("linear", steps, emptyList(), L10n.t("noSolutionsSummary"))
            }
        }
        steps += SolutionStep(L10n.t("moveConstant"), "${fmt(a)}x = ${fmt(-b)}")
        val root = -b / a
        steps += SolutionStep(L10n.t("divideByX"), "x = ${fmt(-b)} / ${fmt(a)} = ${fmt(root)}")
        steps += SolutionStep(L10n.t("verification"), "${fmt(a)} · ${fmt(root)} + ${fmt(b)} = ${fmt(a * root + b)} = 0 ✓")
        return EquationResult("linear", steps, listOf(root), L10n.t("rootSummary", fmt(root)))
    }

    // ---------- Quadratic ----------

    fun solveQuadratic(a: Double, b: Double, c: Double): EquationResult {
        require(a != 0.0) { L10n.t("coefAZero", L10n.t("notAEquation", L10n.t("eqQuadratic"))) }
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep(L10n.t("originalEquation"), fmtQuadratic(a, b, c) + " = 0")

        val d = b * b - 4 * a * c
        steps += SolutionStep(L10n.t("discriminant"), "D = b² − 4ac = ${fmt(b * b)} − 4·${fmt(a)}·${fmt(c)} = ${fmt(d)}")

        val twoA = 2 * a
        return when {
            d > 0 -> {
                steps += SolutionStep(L10n.t("discAnalysis"), L10n.t("discTwoRoots", fmt(d)))
                val x1 = (-b + kotlin.math.sqrt(d)) / twoA
                val x2 = (-b - kotlin.math.sqrt(d)) / twoA
                val exact1 = exactForm(-b, d, twoA, +1)
                val exact2 = exactForm(-b, d, twoA, -1)
                val detail = buildString {
                    append("x = (−b ± √D) / 2a = (${fmt(-b)} ± √${fmt(d)}) / ${fmt(twoA)}")
                    if (exact1 != null) {
                        append("\n" + L10n.t("exactForm", exact1, exact2))
                    }
                    append("\nx₁ ≈ ${fmt(x1)},  x₂ ≈ ${fmt(x2)}")
                }
                steps += SolutionStep(L10n.t("rootFormula"), detail)
                steps += SolutionStep(L10n.t("vieta"), "x₁ + x₂ = −b/a = ${fmt(-b / a)};  x₁·x₂ = c/a = ${fmt(c / a)}")
                steps += SolutionStep(L10n.t("verification"), "a·x₁² + b·x₁ + c = ${fmt(a * x1 * x1 + b * x1 + c)};  a·x₂² + b·x₂ + c = ${fmt(a * x2 * x2 + b * x2 + c)}")
                EquationResult("quadratic", steps, listOf(x1, x2), L10n.t("twoRootsSummary", fmt(x1), fmt(x2)))
            }
            d == 0.0 -> {
                steps += SolutionStep(L10n.t("discAnalysis"), L10n.t("discOneRoot"))
                val x = -b / twoA
                steps += SolutionStep(L10n.t("rootFormulaSingle"), "x = −b / 2a = ${fmt(-b)} / ${fmt(twoA)} = ${fmt(x)}")
                steps += SolutionStep(L10n.t("vieta"), "x₁ + x₂ = −b/a = ${fmt(-b / a)} = 2x ✓;  x₁·x₂ = c/a = ${fmt(c / a)} = x² ✓")
                steps += SolutionStep(L10n.t("verification"), "a·x² + b·x + c = ${fmt(a * x * x + b * x + c)} = 0 ✓")
                EquationResult("quadratic", steps, listOf(x), L10n.t("oneRootSummary", fmt(x)))
            }
            else -> {
                steps += SolutionStep(L10n.t("discAnalysis"), L10n.t("discNoRoots", fmt(d)))
                val re = -b / twoA
                val im = kotlin.math.sqrt(-d) / twoA
                steps += SolutionStep(L10n.t("complexRoots"), "x = ${fmt(re)} ± ${fmt(im)}·i")
                EquationResult("quadratic", steps, emptyList(), L10n.t("noRealRootsSummary"))
            }
        }
    }

    /** Exact form "(p ± q√r) / 2a" when √D can be simplified by factoring out a perfect square. */
    private fun exactForm(minusB: Double, d: Double, twoA: Double, sign: Int): String? {
        if (d <= 0) return null
        val (radicand, sq) = simplifySqrt(d)
        if (sq == 1L) return null // √D is already in simplest form
        val signStr = if (sign > 0) "+" else "−"
        val radical = when {
            radicand == 1.0 -> fmt(sq.toDouble())          // √49 = 7
            sq == 1L -> "√${fmt(radicand)}"                 // √17 = √17
            else -> "${fmt(sq.toDouble())}√${fmt(radicand)}" // √72 = 6√2
        }
        val numerator = "${fmt(minusB)} $signStr $radical"
        return if (twoA < 0) "−($numerator) / ${fmt(-twoA)}" else "($numerator) / ${fmt(twoA)}"
    }

    /** Returns (remaining radicand, perfect-square factor) with perfect-square factor pulled out of √d. */
    private fun simplifySqrt(d: Double): Pair<Double, Long> {
        if (d <= 0 || d != d.toLong().toDouble()) return d to 1L
        var n = d.toLong()
        var sq = 1L
        var i = 2L
        while (i * i <= n) {
            if (n % (i * i) == 0L) {
                n /= i * i
                sq *= i
            } else {
                i++
            }
        }
        return n.toDouble() to sq
    }

    // ---------- Cubic ----------

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): EquationResult {
        require(a != 0.0) { L10n.t("coefAZero", L10n.t("notAEquation", L10n.t("eqCubic"))) }
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep(L10n.t("originalEquation"), fmtCubic(a, b, c, d) + " = 0")

        // 1) Search for an integer rational root among divisors of d/a.
        val candidate = findIntegerRationalRoot(a, b, c, d)
        if (candidate != null) {
            val r = candidate
            steps += SolutionStep(L10n.t("rationalSearch"), L10n.t("rationalFound", fmt(r), fmt(a * r * r * r + b * r * r + c * r + d)))

            // Divide: ax³+bx²+cx+d = (x − r)(ax² + Bx + C)
            val B = b + a * r
            val C = c + B * r // = a r² + b r + c
            steps += SolutionStep(L10n.t("factorization"), L10n.t("factorizationDetail", fmt(r), fmtCubic(a, b, c, d), fmt(r), fmt(a), fmt(B), fmt(C)))

            // Solve quadratic ax² + Bx + C = 0.
            val qr = solveQuadratic(a, B, C)
            steps += SolutionStep(L10n.t("solveQuadraticFactor"), qr.summary)
            qr.steps.forEach { steps += SolutionStep("  ↳ " + it.title, it.detail) }

            val roots = (listOf(r) + qr.roots).sorted()
            val summary = L10n.t("equationRoots") + roots.joinToString(", ") { "x = ${fmt(it)}" }
            steps += SolutionStep(L10n.t("answer"), summary)
            return EquationResult("cubic", steps, roots, summary)
        }

        // 2) No rational root: numeric bisection over a generous range.
        steps += SolutionStep(L10n.t("rationalSearch"), L10n.t("rationalNotFound"))
        val f: (Double) -> Double = { x -> ((a * x + b) * x + c) * x + d }
        val found = mutableListOf<Double>()
        val lo0 = -1000.0
        val hi0 = 1000.0
        val n = 20000
        val h = (hi0 - lo0) / n
        var prevX = lo0
        var prevY = f(prevX)
        for (i in 1..n) {
            val curX = lo0 + i * h
            val curY = f(curX)
            if (prevY == 0.0) addUnique(found, prevX)
            if (prevY * curY < 0) {
                var lo = prevX
                var hi = curX
                var flo = prevY
                repeat(80) {
                    val mid = (lo + hi) / 2
                    val fm = f(mid)
                    if (flo * fm <= 0) hi = mid else { lo = mid; flo = fm }
                }
                addUnique(found, (lo + hi) / 2)
            }
            prevX = curX
            prevY = curY
        }
        if (prevY == 0.0) addUnique(found, prevX)

        return if (found.isEmpty()) {
            steps += SolutionStep(L10n.t("result"), L10n.t("noRootsFound"))
            EquationResult("cubic", steps, emptyList(), L10n.t("numericNotFoundSummary"))
        } else {
            val roots = found.sorted()
            steps += SolutionStep(L10n.t("numericSolution"), L10n.t("approxRootsFound") + roots.joinToString(", ") { "x ≈ ${fmt(it)}" })
            val summary = L10n.t("approxRoots") + roots.joinToString(", ") { "x ≈ ${fmt(it)}" }
            steps += SolutionStep(L10n.t("answer"), summary + L10n.t("numericSuffix"))
            EquationResult("cubic", steps, roots, summary)
        }
    }

    private fun addUnique(list: MutableList<Double>, value: Double) {
        if (list.none { kotlin.math.abs(it - value) < 1e-6 }) list += value
    }

    private fun findIntegerRationalRoot(a: Double, b: Double, c: Double, d: Double): Double? {
        val f: (Double) -> Double = { x -> ((a * x + b) * x + c) * x + d }
        // d/a is the product of roots scaled; integer candidates: divisors of numerator of d/a.
        val ratio = d / a
        val candidates = mutableSetOf<Double>()
        // Try divisors of numerator and denominator-based candidates.
        val dn = ratio.toLong()
        if (ratio == dn.toDouble()) {
            var n = kotlin.math.abs(dn)
            if (n == 0L) candidates += 0.0
            var i = 1L
            while (i * i <= n) {
                if (n % i == 0L) {
                    candidates += i.toDouble(); candidates += (-i).toDouble()
                    candidates += (n / i).toDouble(); candidates += (-(n / i)).toDouble()
                }
                i++
            }
        }
        // Also probe small integers in case of rounding in d/a.
        for (k in -20L..20L) candidates += k.toDouble()
        return candidates
            .filter { f(it).let { v -> kotlin.math.abs(v) < 1e-9 * kotlin.math.max(1.0, kotlin.math.abs(a) + kotlin.math.abs(b) + kotlin.math.abs(c) + kotlin.math.abs(d)) } }
            .minOrNull()
    }

    // ---------- Formatting helpers ----------

    private fun fmt(v: Double): String = Calculator.formatNumber(v)

    private fun fmtQuadratic(a: Double, b: Double, c: Double): String {
        val sb = StringBuilder()
        sb.append(term(a, "x²", includeSign = a < 0))
        sb.append(term(b, "x"))
        sb.append(term(c, ""))
        var text = sb.toString().removePrefix("+ ")
        return text
    }

    private fun fmtCubic(a: Double, b: Double, c: Double, d: Double): String {
        val sb = StringBuilder()
        sb.append(term(a, "x³", includeSign = a < 0))
        sb.append(term(b, "x²"))
        sb.append(term(c, "x"))
        sb.append(term(d, ""))
        return sb.toString().removePrefix("+ ")
    }

    private fun term(coef: Double, power: String, includeSign: Boolean = false): String {
        if (coef == 0.0) return ""
        val sign = if (coef < 0) "− " else "+ "
        val abs = kotlin.math.abs(coef)
        val magnitude = if (abs == 1.0 && power.isNotEmpty()) "" else Calculator.formatNumber(abs)
        return "$sign$magnitude$power "
    }
}
