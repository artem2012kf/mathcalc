package com.example.mathcalc.math

/**
 * Normalizes OCR text from a photo and parses it into a polynomial equation.
 * Supports simple polynomials up to degree 3 in one variable, e.g.
 * "2x+3=7", "x^2-5x+6=0", "x²−5x+6=0", "2х+3=7" (Cyrillic х), "1,5x=3".
 *
 * @return (degree, coefficients from the leading down to the constant term, cleaned equation)
 *         or null when no equation could be parsed.
 */
object PhotoEquationParser {

    private const val MAX_DEGREE = 3

    fun parseEquation(text: String): Triple<Int, List<Double>, String>? {
        val cleaned = normalize(text)
        val parts = cleaned.split('=').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size != 2) return null

        val lhs = parsePolynomialSide(parts[0]) ?: return null
        val rhs = parsePolynomialSide(parts[1]) ?: return null
        if (lhs.isEmpty() && rhs.isEmpty()) return null

        val degree = maxOf(lhs.keys.maxOrNull() ?: 0, rhs.keys.maxOrNull() ?: 0)
        if (degree > MAX_DEGREE || degree < 0) return null

        // Standard form: bring everything to LHS, coefficients from degree down to 0.
        val coefs = (degree downTo 0).map { exp ->
            (lhs[exp] ?: 0.0) - (rhs[exp] ?: 0.0)
        }
        // Trailing zeros on the highest degree are already excluded by `degree`.
        return Triple(degree, coefs, cleaned)
    }

    /** Unifies OCR quirks: symbols, Cyrillic х, commas in numbers, spaces. */
    fun normalize(text: String): String {
        var t = text
            .replace("×", "*")
            .replace("·", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("–", "-")
            .replace("—", "-")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace('х', 'x')
            .replace('Х', 'x')
            .replace('X', 'x')
        // decimal comma between digits → dot
        t = Regex("(?<=\\d),(?=\\d)").replace(t, ".")
        t = t.replace(" ", "").replace("\t", "").replace("\n", "").replace("\r", "")
        return t
    }

    /** Parses one side of the equation into exponent → coefficient map. */
    private fun parsePolynomialSide(side: String): Map<Int, Double>? {
        if (side.isEmpty()) return null
        val terms = Regex("[+-]?[^+-]+").findAll(side)
        val acc = mutableMapOf<Int, Double>()
        for (m in terms) {
            val term = m.value.trim()
            if (term.isEmpty()) return null
            val sign = if (term.startsWith("-")) -1.0 else 1.0
            val body = term.removePrefix("+").removePrefix("-")
            if (body.isEmpty()) return null

            val parsed = parseTerm(body) ?: return null
            val (coef, exp) = parsed
            if (exp > MAX_DEGREE) return null
            acc[exp] = (acc[exp] ?: 0.0) + sign * coef
        }
        return acc
    }

    /**
     * Term body (no sign): "2", "2x", "x", "0.5x", "2*x", "x^2", "2x^2", "2*x^2".
     * Returns (coefficient, exponent) or null.
     */
    private fun parseTerm(body: String): Pair<Double, Int>? {
        if (!body.contains('x')) {
            val c = body.toDoubleOrNull() ?: return null
            return c to 0
        }
        // split into coefficient part and exponent part
        val xi = body.indexOf('x')
        val coefPart = body.substring(0, xi).removeSuffix("*")
        val expPart = body.substring(xi + 1)
        val coef = when {
            coefPart.isEmpty() -> 1.0
            coefPart == "*" -> null
            else -> coefPart.toDoubleOrNull()
        } ?: return null
        val exp = when {
            expPart.isEmpty() -> 1
            else -> {
                val e = expPart.removePrefix("*").removePrefix("^")
                if (e == expPart) return null // garbage after x (not ^, not *)
                e.toIntOrNull() ?: return null
            }
        }
        if (exp < 0) return null
        return coef to exp
    }
}
