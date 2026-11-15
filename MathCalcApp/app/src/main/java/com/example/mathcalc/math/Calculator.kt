package com.example.mathcalc.math

/**
 * Pure-Kotlin expression evaluator with a recursive-descent parser.
 *
 * Grammar:
 *   expression := term (('+' | '-') term)*
 *   term       := factor (('*' | '/') factor)*
 *   factor     := unary ('^' factor)?          // right-associative
 *   unary      := '-' unary | power
 *   power      := primary
 *   primary    := number | constant | 'x' | sqrt | '(' expression ')'
 */
object Calculator {

    private class Parser(private val input: String, private var x: Double) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            skipWhitespace()
            if (pos < input.length) throw IllegalArgumentException("Лишние символы в выражении: «${input.substring(pos)}»")
            return result
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun peek(): Char? {
            skipWhitespace()
            return input.getOrNull(pos)
        }

        private fun consume(expected: Char) {
            skipWhitespace()
            if (pos >= input.length || input[pos] != expected) {
                throw IllegalArgumentException("Ожидался символ «$expected» на позиции ${pos + 1}")
            }
            pos++
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                when (peek()) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                when (peek()) {
                    '*' -> { pos++; value *= parseFactor() }
                    '/' -> {
                        pos++
                        val divisor = parseFactor()
                        value /= divisor
                    }
                    else -> return value
                }
            }
        }

        private fun parseFactor(): Double {
            val base = parseUnary()
            if (peek() == '^') {
                pos++
                return base.pow(parseFactor()) // right-associative
            }
            return base
        }

        private fun parseUnary(): Double {
            return when (peek()) {
                '-' -> { pos++; -parseUnary() }
                '+' -> { pos++; parseUnary() }
                else -> parsePower()
            }
        }

        private fun parsePower(): Double = parsePrimary()

        private fun parsePrimary(): Double {
            skipWhitespace()
            if (pos >= input.length) throw IllegalArgumentException("Неожиданный конец выражения")

            val c = input[pos]

            // sqrt(...) or √(...) or √N
            val func = listOf("sin", "cos", "tan", "sqrt").firstOrNull {
                input.startsWith(it, pos, ignoreCase = true)
            }
            if (func != null && func != "sqrt") {
                pos += func.length
                val arg = if (peek() == '(') {
                    consume('(')
                    val v = parseExpression()
                    consume(')')
                    v
                } else {
                    parsePrimary()
                }
                return when (func.lowercase()) {
                    "sin" -> kotlin.math.sin(arg)
                    "cos" -> kotlin.math.cos(arg)
                    "tan" -> kotlin.math.tan(arg)
                    else -> sqrtChecked(arg)
                }
            }
            if (input.startsWith("sqrt", pos, ignoreCase = true)) {
                val wordEnd = pos + 4
                val after = input.getOrNull(wordEnd)
                if (after == '(') {
                    pos = wordEnd
                    consume('(')
                    val arg = parseExpression()
                    consume(')')
                    return sqrtChecked(arg)
                }
                // sqrt without parentheses: apply to the next primary
                pos = wordEnd
                return sqrtChecked(parsePrimary())
            }
            if (c == '√') {
                pos++
                val arg = if (peek() == '(') {
                    consume('(')
                    val v = parseExpression()
                    consume(')')
                    v
                } else {
                    parsePrimary()
                }
                return sqrtChecked(arg)
            }

            return when {
                c == '(' -> {
                    consume('(')
                    val value = parseExpression()
                    consume(')')
                    value
                }
                c == 'x' || c == 'X' -> { pos++; x }
                c.isDigit() || c == '.' -> parseNumber()
                c == 'π' || input.startsWith("pi", pos, ignoreCase = true) -> {
                    pos += if (c == 'π') 1 else 2
                    kotlin.math.PI
                }
                c == 'e' || c == 'E' -> { pos++; kotlin.math.E }
                else -> throw IllegalArgumentException("Некорректный символ «$c» на позиции ${pos + 1}")
            }
        }

        private fun parseNumber(): Double {
            val start = pos
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            val token = input.substring(start, pos)
            return token.toDoubleOrNull()
                ?: throw IllegalArgumentException("Некорректное число «$token» на позиции ${start + 1}")
        }

        private fun sqrtChecked(arg: Double): Double {
            if (arg < 0) throw IllegalArgumentException("Нельзя извлечь квадратный корень из отрицательного числа: $arg")
            return kotlin.math.sqrt(arg)
        }

        private companion object {
            fun Double.pow(exp: Double) = Math.pow(this, exp)
        }
    }

    /** Evaluates a constant expression (no variable x). */
    fun evaluate(expr: String): Double = evaluate(expr, 0.0)

    /** Evaluates an expression where occurrences of the variable x are bound to [x]. */
    fun evaluate(expr: String, x: Double): Double {
        if (expr.isBlank()) throw IllegalArgumentException("Выражение не может быть пустым")
        val value = Parser(expr, x).parse()
        if (value.isNaN() || value.isInfinite()) {
            throw IllegalArgumentException("Результат выражения не определён")
        }
        return value
    }

    /** Formats a number: up to 10 significant decimals, trailing zeros trimmed, integer point values without a dot. */
    fun formatNumber(d: Double): String {
        if (d.isNaN()) return "NaN"
        if (d.isInfinite()) return if (d > 0) "∞" else "-∞"
        val rounded = String.format(java.util.Locale.US, "%.10g", d).toDouble()
        // Round to 10 decimal places, then trim.
        var text = java.math.BigDecimal(rounded).setScale(10, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        if (text.contains('.')) {
            text = text.trimEnd('0').trimEnd('.')
        }
        if (text == "-0") text = "0"
        return text
    }
}
