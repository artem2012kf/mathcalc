package com.example.mathcalc.math

data class SolutionStep(val title: String, val detail: String)

data class EquationResult(
    val type: String,
    val steps: List<SolutionStep>,
    val roots: List<Double>,
    val summary: String,
)

object EquationSolver {

    fun solve(type: String, coefs: List<Double>): EquationResult = when (type.lowercase()) {
        "linear" -> {
            require(coefs.size >= 2) { "Для линейного уравнения нужно 2 коэффициента" }
            solveLinear(coefs[0], coefs[1])
        }
        "quadratic" -> {
            require(coefs.size >= 3) { "Для квадратного уравнения нужно 3 коэффициента" }
            solveQuadratic(coefs[0], coefs[1], coefs[2])
        }
        "cubic" -> {
            require(coefs.size >= 4) { "Для кубического уравнения нужно 4 коэффициента" }
            solveCubic(coefs[0], coefs[1], coefs[2], coefs[3])
        }
        else -> throw IllegalArgumentException("Неизвестный тип уравнения: $type")
    }

    // ---------- Linear ----------

    fun solveLinear(a: Double, b: Double): EquationResult {
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep("Исходное уравнение", "${fmt(a)}x + ${fmt(b)} = 0")
        if (a == 0.0) {
            return if (b == 0.0) {
                steps += SolutionStep("Анализ", "a = 0 и b = 0 — уравнение верно при любом x")
                EquationResult("linear", steps, emptyList(), "Уравнение имеет бесконечно много решений (тождество)")
            } else {
                steps += SolutionStep("Анализ", "a = 0 — уравнение ${fmt(b)} = 0 не имеет решений")
                EquationResult("linear", steps, emptyList(), "Уравнение не имеет решений")
            }
        }
        steps += SolutionStep("Переносим свободный член", "${fmt(a)}x = ${fmt(-b)}")
        val root = -b / a
        steps += SolutionStep("Делим на коэффициент при x", "x = ${fmt(-b)} / ${fmt(a)} = ${fmt(root)}")
        steps += SolutionStep("Проверка подстановкой", "${fmt(a)} · ${fmt(root)} + ${fmt(b)} = ${fmt(a * root + b)} = 0 ✓")
        return EquationResult("linear", steps, listOf(root), "Корень уравнения: x = ${fmt(root)}")
    }

    // ---------- Quadratic ----------

    fun solveQuadratic(a: Double, b: Double, c: Double): EquationResult {
        require(a != 0.0) { "Коэффициент a не может быть равен нулю (это не квадратное уравнение)" }
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep("Исходное уравнение", fmtQuadratic(a, b, c) + " = 0")

        val d = b * b - 4 * a * c
        steps += SolutionStep("Дискриминант", "D = b² − 4ac = ${fmt(b * b)} − 4·${fmt(a)}·${fmt(c)} = ${fmt(d)}")

        val twoA = 2 * a
        return when {
            d > 0 -> {
                steps += SolutionStep("Анализ дискриминанта", "D = ${fmt(d)} > 0 → два корня")
                val x1 = (-b + kotlin.math.sqrt(d)) / twoA
                val x2 = (-b - kotlin.math.sqrt(d)) / twoA
                val exact1 = exactForm(-b, d, twoA, +1)
                val exact2 = exactForm(-b, d, twoA, -1)
                val detail = buildString {
                    append("x = (−b ± √D) / 2a = (${fmt(-b)} ± √${fmt(d)}) / ${fmt(twoA)}")
                    if (exact1 != null) {
                        append("\nТочная форма: x₁ = $exact1, x₂ = $exact2")
                    }
                    append("\nx₁ ≈ ${fmt(x1)},  x₂ ≈ ${fmt(x2)}")
                }
                steps += SolutionStep("Формула корней", detail)
                steps += SolutionStep("Теорема Виета", "x₁ + x₂ = −b/a = ${fmt(-b / a)} (проверка: ${fmt(x1 + x2)});  x₁·x₂ = c/a = ${fmt(c / a)} (проверка: ${fmt(x1 * x2)})")
                steps += SolutionStep("Проверка подстановкой", "a·x₁² + b·x₁ + c = ${fmt(a * x1 * x1 + b * x1 + c)};  a·x₂² + b·x₂ + c = ${fmt(a * x2 * x2 + b * x2 + c)}")
                EquationResult("quadratic", steps, listOf(x1, x2), "Два корня: x₁ = ${fmt(x1)}, x₂ = ${fmt(x2)}")
            }
            d == 0.0 -> {
                steps += SolutionStep("Анализ дискриминанта", "D = 0 → один (двойной) корень")
                val x = -b / twoA
                steps += SolutionStep("Формула корня", "x = −b / 2a = ${fmt(-b)} / ${fmt(twoA)} = ${fmt(x)}")
                steps += SolutionStep("Теорема Виета", "x₁ + x₂ = −b/a = ${fmt(-b / a)} = 2x ✓;  x₁·x₂ = c/a = ${fmt(c / a)} = x² ✓")
                steps += SolutionStep("Проверка подстановкой", "a·x² + b·x + c = ${fmt(a * x * x + b * x + c)} = 0 ✓")
                EquationResult("quadratic", steps, listOf(x), "Один корень (кратный): x = ${fmt(x)}")
            }
            else -> {
                steps += SolutionStep("Анализ дискриминанта", "D = ${fmt(d)} < 0 → действительных корней нет")
                val re = -b / twoA
                val im = kotlin.math.sqrt(-d) / twoA
                steps += SolutionStep("Комплексные корни (справочно)", "x = ${fmt(re)} ± ${fmt(im)}·i")
                EquationResult("quadratic", steps, emptyList(), "Действительных корней нет (D < 0)")
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
        require(a != 0.0) { "Коэффициент a не может быть равен нулю (это не кубическое уравнение)" }
        val steps = mutableListOf<SolutionStep>()
        steps += SolutionStep("Исходное уравнение", fmtCubic(a, b, c, d) + " = 0")

        // 1) Search for an integer rational root among divisors of d/a.
        val candidate = findIntegerRationalRoot(a, b, c, d)
        if (candidate != null) {
            val r = candidate
            steps += SolutionStep("Поиск рационального корня", "Целый корень ищем среди делителей свободного члена d/a. Найден: x = ${fmt(r)} (проверка: a·r³+b·r²+c·r+d = ${fmt(a * r * r * r + b * r * r + c * r + d)} = 0 ✓)")

            // Divide: ax³+bx²+cx+d = (x − r)(ax² + Bx + C)
            val B = b + a * r
            val C = c + B * r // = a r² + b r + c
            steps += SolutionStep("Разложение на множители", "Делим на (x − ${fmt(r)}):  ${fmtCubic(a, b, c, d)} = (x − ${fmt(r)})(${fmt(a)}x² + ${fmt(B)}x + ${fmt(C)})")

            // Solve quadratic ax² + Bx + C = 0.
            val qr = solveQuadratic(a, B, C)
            steps += SolutionStep("Решаем квадратный множитель", qr.summary)
            qr.steps.forEach { steps += SolutionStep("  ↳ " + it.title, it.detail) }

            val roots = (listOf(r) + qr.roots).sorted()
            val summary = "Корни уравнения: " + roots.joinToString(", ") { "x = ${fmt(it)}" }
            steps += SolutionStep("Ответ", summary)
            return EquationResult("cubic", steps, roots, summary)
        }

        // 2) No rational root: numeric bisection over a generous range.
        steps += SolutionStep("Поиск рационального корня", "Целого рационального корня среди делителей свободного члена не найдено — применяем численный метод (бисекция)")
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
            steps += SolutionStep("Результат", "На интервале [−1000, 1000] действительных корней не обнаружено")
            EquationResult("cubic", steps, emptyList(), "Действительные корни численным методом не найдены (возможно, они комплексные или вне диапазона)")
        } else {
            val roots = found.sorted()
            steps += SolutionStep("Численное решение (метод бисекции)", "Найдены приближённые корни: " + roots.joinToString(", ") { "x ≈ ${fmt(it)}" })
            val summary = "Приближённые корни: " + roots.joinToString(", ") { "x ≈ ${fmt(it)}" }
            steps += SolutionStep("Ответ", summary + " (численное решение)")
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
