package com.example.mathcalc.math

import kotlin.math.abs

/**
 * Rational number with Long numerator/denominator, always normalized:
 * denominator > 0 and gcd(num, den) == 1. Instances must be created via the
 * companion `Fraction(n, d)` invoke which performs normalization.
 */
data class Fraction private constructor(val num: Long, val den: Long) {

    companion object {
        private fun gcd(a: Long, b: Long): Long {
            var x = abs(a)
            var y = abs(b)
            while (y != 0L) {
                val t = x % y
                x = y
                y = t
            }
            return if (x == 0L) 1L else x
        }

        /** Normalizing factory: den != 0; sign goes to the numerator, gcd is reduced. */
        operator fun invoke(num: Long, den: Long = 1L): Fraction {
            require(den != 0L) { "Denominator cannot be zero" }
            var n = num
            var d = den
            if (d < 0) {
                n = -n
                d = -d
            }
            val g = gcd(n, d)
            return Fraction(n / g, d / g)
        }

        fun gcdOf(a: Long, b: Long): Long = gcd(a, b)

        fun lcmOf(a: Long, b: Long): Long {
            if (a == 0L || b == 0L) return 0L
            return abs(a / gcd(a, b) * b)
        }
    }

    val isInteger: Boolean get() = den == 1L

    operator fun unaryMinus(): Fraction = Fraction(-num, den)

    operator fun plus(other: Fraction): Fraction = Fraction(num * other.den + other.num * den, den * other.den)
    operator fun minus(other: Fraction): Fraction = Fraction(num * other.den - other.num * den, den * other.den)
    operator fun times(other: Fraction): Fraction = Fraction(num * other.num, den * other.den)

    operator fun div(other: Fraction): Fraction {
        require(other.num != 0L) { "Division by zero fraction" }
        return Fraction(num * other.den, den * other.num)
    }

    fun toDecimal(): Double = num.toDouble() / den.toDouble()

    /** "3/4", "2", or mixed "1 1/2". */
    override fun toString(): String {
        if (den == 1L) return num.toString()
        val whole = abs(num) / den
        return if (whole == 0L) {
            "$num/$den"
        } else {
            val sign = if (num < 0) "-" else ""
            val rest = abs(num) % den
            "$sign$whole $rest/$den"
        }
    }
}

/** Fraction arithmetic with full step-by-step solution. */
object FractionSolver {

    fun solveFractionExample(a: Fraction, b: Fraction, op: Char): EquationResult {
        val steps = mutableListOf<SolutionStep>()
        val opStr = when (op) {
            '+' -> "+"
            '-' -> "−"
            '*' -> "·"
            '/' -> ":"
            else -> throw IllegalArgumentException("Unknown operation: $op")
        }
        steps += SolutionStep(L10n.t("fracOriginal"), "${a} $opStr $b")

        val res: Fraction = when (op) {
            '+', '-' -> {
                val lcm = Fraction.lcmOf(a.den, b.den)
                val aScaled = a.num * (lcm / a.den)
                val bScaled = b.num * (lcm / b.den)
                steps += SolutionStep(
                    L10n.t("fracCommonDenominator"),
                    "${L10n.t("lcmWord")}(${a.den}, ${b.den}) = $lcm:  " +
                        "${a} → ${Fraction(aScaled, lcm)},  ${b} → ${Fraction(bScaled, lcm)}"
                )
                val r = if (op == '+') Fraction(aScaled + bScaled, lcm) else Fraction(aScaled - bScaled, lcm)
                steps += SolutionStep(L10n.t("fracSumNums"), "${Fraction(aScaled, lcm)} $opStr ${Fraction(bScaled, lcm)} = $r")
                r
            }
            '*' -> {
                steps += SolutionStep(
                    L10n.t("fracCrossMultiply"),
                    L10n.t("fracCrossDetail", a.num, b.num, a.den, b.den, "${a.num * b.num}/${a.den * b.den}")
                )
                a * b
            }
            '/' -> {
                require(b.num != 0L) { L10n.t("fracDivByZero") }
                steps += SolutionStep(
                    L10n.t("fracInvert"),
                    L10n.t("fracInvertDetail", b.toString(), Fraction(b.den, b.num).toString())
                )
                steps += SolutionStep(
                    L10n.t("fracMultiply"),
                    "${a} · ${Fraction(b.den, b.num)} = ${a.num * b.den}/${a.den * b.num}"
                )
                a / b
            }
            else -> throw IllegalArgumentException("Unknown operation: $op")
        }

        val final = finalResult(res)
        steps += SolutionStep(L10n.t("result"), final)
        return EquationResult("fraction", steps, emptyList(), final)
    }

    /** "5/6 ≈ 0.8333333" / "1 1/2 ≈ 1.5" / "2 ≈ 2". */
    private fun finalResult(res: Fraction): String {
        val dec = res.toDecimal()
        return if (res.isInteger) res.toString()
        else "${res} ≈ ${Calculator.formatNumber(dec)}"
    }
}

/** Ready-made step-by-step examples: percents, divisibility, powers. */
object Examples {

    /** p% of x. */
    fun percentageOf(x: Double, p: Double): EquationResult {
        val steps = mutableListOf<SolutionStep>()
        val frac = p / 100.0
        steps += SolutionStep(
            L10n.t("percentStepOf"),
            L10n.t("percentOfDetail1", Calculator.formatNumber(p), Calculator.formatNumber(p), Calculator.formatNumber(frac))
        )
        val res = frac * x
        steps += SolutionStep(
            L10n.t("percentMultiply"),
            "${Calculator.formatNumber(frac)} · ${Calculator.formatNumber(x)} = ${Calculator.formatNumber(res)}"
        )
        val summary = L10n.t("percentOfSummary", Calculator.formatNumber(p), Calculator.formatNumber(x), Calculator.formatNumber(res))
        steps += SolutionStep(L10n.t("answer"), summary)
        return EquationResult("percent", steps, emptyList(), summary)
    }

    /** x is what percent of y. */
    fun percentOfWhat(x: Double, y: Double): EquationResult {
        require(y != 0.0) { L10n.t("percentDivByZero") }
        val steps = mutableListOf<SolutionStep>()
        val ratio = x / y
        steps += SolutionStep(
            L10n.t("percentStepRatio"),
            "${Calculator.formatNumber(x)} / ${Calculator.formatNumber(y)} = ${Calculator.formatNumber(ratio)}"
        )
        val res = ratio * 100.0
        steps += SolutionStep(
            L10n.t("percentStepTimes100"),
            "${Calculator.formatNumber(ratio)} · 100 = ${Calculator.formatNumber(res)}%"
        )
        val summary = L10n.t("percentOfWhatSummary", Calculator.formatNumber(x), Calculator.formatNumber(y), Calculator.formatNumber(res))
        steps += SolutionStep(L10n.t("answer"), summary)
        return EquationResult("percent", steps, emptyList(), summary)
    }

    /** GCD via Euclid's algorithm + LCM via a·b/gcd. */
    fun gcdLcm(a: Long, b: Long): EquationResult {
        val steps = mutableListOf<SolutionStep>()
        var x = abs(a)
        var y = abs(b)
        steps += SolutionStep(
            L10n.t("euclidStep"),
            "${L10n.t("euclidIntro")} ${x}, ${y}"
        )
        while (y != 0L) {
            val q = x / y
            val r = x % y
            steps += SolutionStep(L10n.t("euclidDivide"), L10n.t("euclidDetail", x, y, q, r))
            x = y
            y = r
        }
        val gcd = x
        val lcm = if (gcd == 0L) 0L else abs(a) / gcd * abs(b)
        val gcdSummary = "${L10n.t("gcdWord")}(${a}, ${b}) = $gcd"
        steps += SolutionStep(L10n.t("result"), "$gcdSummary;  ${L10n.t("lcmWord")}(${a}, ${b}) = ${a}·${b}/$gcd = $lcm")
        val summary = "$gcdSummary,  ${L10n.t("lcmWord")}(${a}, ${b}) = $lcm"
        steps += SolutionStep(L10n.t("answer"), summary)
        return EquationResult("divisibility", steps, emptyList(), summary)
    }

    /** Prime factorization of n > 0. */
    fun primeFactorization(n: Long): EquationResult {
        require(n >= 2) { L10n.t("primeNeedTwo") }
        val steps = mutableListOf<SolutionStep>()
        val factors = mutableListOf<Int>()
        var rest = n
        var p = 2
        while (p * p <= rest) {
            while (rest % p == 0L) {
                steps += SolutionStep(L10n.t("primeStep"), L10n.t("primeStepDetail", rest, p, rest / p))
                factors += p
                rest /= p
            }
            p++
        }
        if (rest > 1) {
            steps += SolutionStep(L10n.t("primeStep"), L10n.t("primeStepDetail", rest, rest, 1))
            factors += rest.toInt()
        }
        val pretty = formatFactors(factors)
        val summary = L10n.t("primeResult", n, pretty)
        steps += SolutionStep(L10n.t("answer"), summary)
        return EquationResult("divisibility", steps, emptyList(), summary)
    }

    /** Exponentiation with repeated multiplication steps. */
    fun powerOf(x: Long, n: Int): EquationResult {
        require(n >= 0) { L10n.t("powerNeedNonNegative") }
        val steps = mutableListOf<SolutionStep>()
        var acc = 1L
        repeat(n) {
            acc *= x
            steps += SolutionStep(L10n.t("powerStep"), "$acc")
        }
        if (n == 0) {
            steps += SolutionStep(L10n.t("powerStep"), "${x}^0 = 1")
        }
        val summary = "${x}^$n = $acc"
        steps += SolutionStep(L10n.t("answer"), summary)
        return EquationResult("power", steps, emptyList(), summary)
    }

    /** "12 = 2² · 3" style formatting (exponents shown as superscript digits). */
    private fun formatFactors(factors: List<Int>): String {
        if (factors.isEmpty()) return "1"
        val sup = mapOf('0' to "⁰", '1' to "¹", '2' to "²", '3' to "³", '4' to "⁴", '5' to "⁵", '6' to "⁶", '7' to "⁷", '8' to "⁸", '9' to "⁹")
        val groups = factors.groupingBy { it }.eachCount()
        return groups.entries.sortedBy { it.key }.joinToString(" · ") { (prime, count) ->
            if (count == 1) "$prime" else "$prime${count.toString().map { sup[it] }.joinToString("")}"
        }
    }
}
