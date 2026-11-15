# MathCalc — Калькулятор с подробными решениями

Android-приложение (Kotlin + Jetpack Compose) + MCP-сервер.

## Возможности
- **Калькулятор**: арифметика, `√`, `^`, скобки, история вычислений.
- **Уравнения** с подробным пошаговым решением на русском:
  - линейные `ax+b=0`
  - квадратные `ax²+bx+c=0` (дискриминант, Виета, проверка, точные корни с упрощённым `√D`)
  - кубические `ax³+bx²+cx+d=0` (рациональные корни, разложение на множители, численный метод)
- **График** `y=f(x)`: оси, сетка, нули функции, перетаскивание и зум.

## Сборка APK
```bash
cd MathCalcApp
set JAVA_HOME=C:\Users\Artem\.jdks\jbr-21.0.11
gradlew.bat assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`. Установка: `adb install app-debug.apk`.
Проект также открывается в Android Studio (File → Open → MathCalcApp).

## MCP-сервер (mathcalc-mcp)
Инструменты:
- `solve_equation(equation)` — например `"x^2-5x+6=0"` → пошаговое решение;
- `evaluate_expression(expression)` — вычисление выражений с `√`, `^`, скобками;
- `plot_function(expression, x_min, x_max)` — точки кривой и нули функции (JSON).

Подключение к клиенту (JSON-конфиг mcpServers):
```json
{
  "mcpServers": {
    "mathcalc": {
      "command": "node",
      "args": ["<путь>/mathcalc-mcp/index.js"]
    }
  }
}
```

## Структура
```
MathCalcApp/   — Android-проект (app/src/main/java/com/example/mathcalc: math/ — логика, ui/ — экраны)
mathcalc-mcp/  — MCP-сервер на Node.js
```
