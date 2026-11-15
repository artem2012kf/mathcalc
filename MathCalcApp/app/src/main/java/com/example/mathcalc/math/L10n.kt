package com.example.mathcalc.math

import java.util.Locale

/**
 * Lightweight localization for math logic (pure Kotlin, no Android deps).
 * The active language is set from AppSettings when the app starts and on change.
 */
object L10n {

    /** Current language: "ru" (default) or "en". */
    var lang: String = "ru"

    private val ru: Map<String, String> = mapOf(
        // ---------- Calculator errors ----------
        "extraSymbols" to "Лишние символы в выражении: «%s»",
        "expectedChar" to "Ожидался символ «%s» на позиции %d",
        "unexpectedEnd" to "Неожиданный конец выражения",
        "invalidChar" to "Некорректный символ «%s» на позиции %d",
        "invalidNumber" to "Некорректное число «%s» на позиции %d",
        "sqrtNegative" to "Нельзя извлечь квадратный корень из отрицательного числа: %s",
        "emptyExpr" to "Выражение не может быть пустым",
        "undefinedResult" to "Результат выражения не определён",
        "needPointsPlot" to "Нужно минимум 2 точки для построения графика",
        "needPointsZeros" to "Нужно минимум 2 точки для поиска корней",

        // ---------- EquationSolver ----------
        "needCoefs" to "Для %s уравнения нужно %d коэффициента",
        "unknownType" to "Неизвестный тип уравнения: %s",
        "eqLinear" to "линейного",
        "eqQuadratic" to "квадратного",
        "eqCubic" to "кубического",
        "notAEquation" to "это не %s уравнение",

        "originalEquation" to "Исходное уравнение",
        "analysis" to "Анализ",
        "linearIdentityDetail" to "a = 0 и b = 0 — уравнение верно при любом x",
        "linearNoRootsDetail" to "a = 0 — уравнение %s = 0 не имеет решений",
        "identitySummary" to "Уравнение имеет бесконечно много решений (тождество)",
        "noSolutionsSummary" to "Уравнение не имеет решений",
        "moveConstant" to "Переносим свободный член",
        "divideByX" to "Делим на коэффициент при x",
        "verification" to "Проверка подстановкой",
        "rootSummary" to "Корень уравнения: x = %s",

        "coefAZero" to "Коэффициент a не может быть равен нулю (%s)",
        "discriminant" to "Дискриминант",
        "discAnalysis" to "Анализ дискриминанта",
        "discTwoRoots" to "D = %s > 0 → два корня",
        "exactForm" to "Точная форма: x₁ = %s, x₂ = %s",
        "rootFormula" to "Формула корней",
        "vieta" to "Теорема Виета",
        "twoRootsSummary" to "Два корня: x₁ = %s, x₂ = %s",
        "discOneRoot" to "D = 0 → один (двойной) корень",
        "rootFormulaSingle" to "Формула корня",
        "oneRootSummary" to "Один корень (кратный): x = %s",
        "discNoRoots" to "D = %s < 0 → действительных корней нет",
        "complexRoots" to "Комплексные корни (справочно)",
        "noRealRootsSummary" to "Действительных корней нет (D < 0)",

        "rationalSearch" to "Поиск рационального корня",
        "rationalFound" to "Целый корень ищем среди делителей свободного члена d/a. Найден: x = %s (проверка: a·r³+b·r²+c·r+d = %s = 0 ✓)",
        "factorization" to "Разложение на множители",
        "factorizationDetail" to "Делим на (x − %s):  %s = (x − %s)(%sx² + %sx + %s)",
        "solveQuadraticFactor" to "Решаем квадратный множитель",
        "equationRoots" to "Корни уравнения: ",
        "answer" to "Ответ",
        "rationalNotFound" to "Целого рационального корня среди делителей свободного члена не найдено — применяем численный метод (бисекция)",
        "result" to "Результат",
        "noRootsFound" to "На интервале [−1000, 1000] действительных корней не обнаружено",
        "numericNotFoundSummary" to "Действительные корни численным методом не найдены (возможно, они комплексные или вне диапазона)",
        "numericSolution" to "Численное решение (метод бисекции)",
        "approxRootsFound" to "Найдены приближённые корни: ",
        "approxRoots" to "Приближённые корни: ",
        "numericSuffix" to " (численное решение)",

        // ---------- UI strings ----------
        "appTitle" to "MathCalc",
        "navCalc" to "Калькулятор",
        "navEquations" to "Уравнения",
        "navGraph" to "График",
        "errorInExpression" to "Ошибка в выражении",
        "history" to "История",
        "solveEquationsTitle" to "Решение уравнений",
        "eqLinearShort" to "Линейное",
        "eqQuadraticShort" to "Квадратное",
        "eqCubicShort" to "Кубическое",
        "eqLinearFull" to "Линейное ax+b=0",
        "eqQuadraticFull" to "Квадратное ax²+bx+c=0",
        "eqCubicFull" to "Кубическое ax³+bx²+cx+d=0",
        "solve" to "Решить",
        "roots" to "Корни:",
        "solveFailed" to "Не удалось решить уравнение: %s",
        "unknownError" to "неизвестная ошибка",
        "graphTitle" to "График функции",
        "functionLabel" to "Функция y = f(x)",
        "xMin" to "xMin",
        "xMax" to "xMax",
        "plot" to "Построить",
        "zoomIn" to "Приблизить",
        "zoomOut" to "Отдалить",
        "reset" to "Сброс",
        "resetZoom" to "Сбросить масштаб",
        "zerosOn" to "Нули функции на [%s; %s]:"
    )

    private val en: Map<String, String> = mapOf(
        // ---------- Calculator errors ----------
        "extraSymbols" to "Extra symbols in expression: «%s»",
        "expectedChar" to "Expected character «%s» at position %d",
        "unexpectedEnd" to "Unexpected end of expression",
        "invalidChar" to "Invalid character «%s» at position %d",
        "invalidNumber" to "Invalid number «%s» at position %d",
        "sqrtNegative" to "Cannot take the square root of a negative number: %s",
        "emptyExpr" to "Expression cannot be empty",
        "undefinedResult" to "The expression result is undefined",
        "needPointsPlot" to "At least 2 points are required to plot the graph",
        "needPointsZeros" to "At least 2 points are required to find zeros",

        // ---------- EquationSolver ----------
        "needCoefs" to "The %s equation requires %d coefficients",
        "unknownType" to "Unknown equation type: %s",
        "eqLinear" to "linear",
        "eqQuadratic" to "quadratic",
        "eqCubic" to "cubic",
        "notAEquation" to "this is not a %s equation",

        "originalEquation" to "Original equation",
        "analysis" to "Analysis",
        "linearIdentityDetail" to "a = 0 and b = 0 — the equation holds for any x",
        "linearNoRootsDetail" to "a = 0 — the equation %s = 0 has no solutions",
        "identitySummary" to "The equation has infinitely many solutions (identity)",
        "noSolutionsSummary" to "The equation has no solutions",
        "moveConstant" to "Move the constant term",
        "divideByX" to "Divide by the coefficient of x",
        "verification" to "Verification by substitution",
        "rootSummary" to "Equation root: x = %s",

        "coefAZero" to "Coefficient a cannot be equal to zero (%s)",
        "discriminant" to "Discriminant",
        "discAnalysis" to "Discriminant analysis",
        "discTwoRoots" to "D = %s > 0 → two roots",
        "exactForm" to "Exact form: x₁ = %s, x₂ = %s",
        "rootFormula" to "Root formula",
        "vieta" to "Vieta's theorem",
        "twoRootsSummary" to "Two roots: x₁ = %s, x₂ = %s",
        "discOneRoot" to "D = 0 → one (double) root",
        "rootFormulaSingle" to "Root formula",
        "oneRootSummary" to "One root (repeated): x = %s",
        "discNoRoots" to "D = %s < 0 → no real roots",
        "complexRoots" to "Complex roots (for reference)",
        "noRealRootsSummary" to "No real roots (D < 0)",

        "rationalSearch" to "Rational root search",
        "rationalFound" to "An integer root is sought among the divisors of the constant term d/a. Found: x = %s (check: a·r³+b·r²+c·r+d = %s = 0 ✓)",
        "factorization" to "Factoring",
        "factorizationDetail" to "Dividing by (x − %s):  %s = (x − %s)(%sx² + %sx + %s)",
        "solveQuadraticFactor" to "Solving the quadratic factor",
        "equationRoots" to "Equation roots: ",
        "answer" to "Answer",
        "rationalNotFound" to "No integer rational root among the divisors of the constant term — using a numerical method (bisection)",
        "result" to "Result",
        "noRootsFound" to "No real roots found on the interval [−1000, 1000]",
        "numericNotFoundSummary" to "Real roots were not found numerically (they may be complex or outside the range)",
        "numericSolution" to "Numerical solution (bisection method)",
        "approxRootsFound" to "Approximate roots found: ",
        "approxRoots" to "Approximate roots: ",
        "numericSuffix" to " (numerical solution)",

        // ---------- UI strings ----------
        "appTitle" to "MathCalc",
        "navCalc" to "Calculator",
        "navEquations" to "Equations",
        "navGraph" to "Graph",
        "errorInExpression" to "Error in expression",
        "history" to "History",
        "solveEquationsTitle" to "Solving equations",
        "eqLinearShort" to "Linear",
        "eqQuadraticShort" to "Quadratic",
        "eqCubicShort" to "Cubic",
        "eqLinearFull" to "Linear ax+b=0",
        "eqQuadraticFull" to "Quadratic ax²+bx+c=0",
        "eqCubicFull" to "Cubic ax³+bx²+cx+d=0",
        "solve" to "Solve",
        "roots" to "Roots:",
        "solveFailed" to "Failed to solve the equation: %s",
        "unknownError" to "unknown error",
        "graphTitle" to "Function graph",
        "functionLabel" to "Function y = f(x)",
        "xMin" to "xMin",
        "xMax" to "xMax",
        "plot" to "Plot",
        "zoomIn" to "Zoom in",
        "zoomOut" to "Zoom out",
        "reset" to "Reset",
        "resetZoom" to "Reset zoom",
        "zerosOn" to "Zeros of the function on [%s; %s]:"
    )

    /** Returns the localized string for [key], formatted with [args] if provided. */
    fun t(key: String, vararg args: Any?): String {
        val template = if (lang == "en") (en[key] ?: ru[key] ?: key) else (ru[key] ?: key)
        return if (args.isEmpty()) template else String.format(Locale.US, template, *args)
    }
}
