// index.js — MCP-сервер калькулятора (stdio)
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";
import { evaluate, formatNumber, solve, sample, zeros } from "./solver.js";

const server = new McpServer({
  name: "mathcalc-mcp",
  version: "1.0.0",
});

// ---------- Разбор простого многочлена по x (до степени 3) ----------
// Возвращает коэффициенты [a3, a2, a1, a0]
export function parsePolynomial(text) {
  let s = String(text).toLowerCase().replace(/\s+/g, "");
  if (s === "") throw new Error("Пустое выражение");
  s = s.replace(/−/g, "-").replace(/·/g, "*").replace(/\*\*/g, "^");

  const coefs = [0, 0, 0, 0]; // [a0, a1, a2, a3]
  let sign = 1;
  let i = 0;
  let parsedTerm = false;

  function readNumber() {
    const m = /^\d+(\.\d+)?/.exec(s.slice(i));
    if (!m) return null;
    i += m[0].length;
    return parseFloat(m[0]);
  }

  while (i < s.length) {
    // знак
    let termSign = sign;
    while (i < s.length && (s[i] === "+" || s[i] === "-")) {
      if (s[i] === "-") termSign = -termSign;
      i++;
    }
    if (i >= s.length) throw new Error("Выражение заканчивается знаком операции");

    // числовой коэффициент
    let coef = readNumber();
    let hasCoef = coef !== null;
    if (!hasCoef) coef = 1;

    // x / x^k
    let power = 0;
    if (i < s.length && s[i] === "x") {
      i++;
      power = 1;
      if (i < s.length && s[i] === "^") {
        i++;
        const p = readNumber();
        if (p === null || !Number.isInteger(p) || p < 0 || p > 3) {
          throw new Error("Поддерживаются только целые степени x от 0 до 3");
        }
        power = p;
      }
    } else if (!hasCoef) {
      throw new Error(`Некорректный символ «${s[i]}» в позиции ${i + 1}`);
    }

    if (power === 0 && hasCoef && i < s.length && s[i] === "x") {
      // случай "2x" уже обработан выше — здесь быть не должно
      throw new Error("Ошибка разбора выражения");
    }

    coefs[power] += termSign * coef;
    parsedTerm = true;
    sign = 1;
  }

  if (!parsedTerm) throw new Error("Не удалось распознать многочлен");
  return coefs; // [a0, a1, a2, a3]
}

function coefsText([a0, a1, a2, a3]) {
  const parts = [];
  if (a3) parts.push(`${formatNumber(a3)}x³`);
  if (a2) parts.push(`${a2 >= 0 && parts.length ? "+" : ""}${formatNumber(a2)}x²`);
  if (a1) parts.push(`${a1 >= 0 && parts.length ? "+" : ""}${formatNumber(a1)}x`);
  if (a0 || parts.length === 0) parts.push(`${a0 >= 0 && parts.length ? "+" : ""}${formatNumber(a0)}`);
  return parts.join(" ").replace(/^\+/, "");
}

function formatSolution(result) {
  const lines = [`ИТОГ: ${result.summary}`, ""];
  result.steps.forEach((s, idx) => {
    lines.push(`${idx + 1}. ${s.title}`);
    lines.push(`   ${s.detail}`);
    lines.push("");
  });
  if (result.roots.length) {
    lines.push("КОРНИ:");
    result.roots.forEach((r, i) => {
      lines.push(`  x${i + 1} = ${r.decimal}${r.exact && r.exact !== r.decimal ? ` (точно: ${r.exact})` : ""}`);
    });
  } else if (result.type !== "identity") {
    lines.push("КОРНИ: действительных корней нет.");
  }
  return lines.join("\n").replace(/\n{3,}/g, "\n\n");
}

// ---------- Инструмент: решение уравнения ----------
server.tool(
  "solve_equation",
  "Решает уравнение вида «2x+3=7», «x^2-5x+6=0» или «x^3-6x^2+11x-6=0». Возвращает подробное пошаговое решение на русском языке (дискриминант, формула корней, проверка подстановкой, теорема Виета).",
  { equation: z.string().describe("Уравнение, например «x^2-5x+6=0»") },
  async ({ equation }) => {
    try {
      const eq = String(equation).replace(/\s+/g, "");
      const parts = eq.split("=");
      if (parts.length !== 2) {
        throw new Error("Уравнение должно содержать ровно один знак «=», например «x^2-5x+6=0»");
      }
      const lhs = parsePolynomial(parts[0]);
      const rhs = parsePolynomial(parts[1]);
      const coefs = [0, 1, 2, 3].map(k => lhs[k] - rhs[k]); // [a0, a1, a2, a3]

      let kind, sc;
      if (coefs[3] !== 0) {
        kind = "cubic";
        sc = [coefs[3], coefs[2], coefs[1], coefs[0]];
      } else if (coefs[2] !== 0) {
        kind = "quadratic";
        sc = [coefs[2], coefs[1], coefs[0]];
      } else {
        kind = "linear";
        sc = [coefs[1], coefs[0]];
      }

      const result = solve(kind, sc);
      const stdForm = coefsText(coefs);
      const text =
        `Уравнение: ${eq}\n` +
        `Степень уравнения: ${kind === "cubic" ? "3 (кубическое)" : kind === "quadratic" ? "2 (квадратное)" : "1 (линейное)"}\n` +
        `Стандартный вид: ${stdForm} = 0\n\n` +
        formatSolution(result);
      return { content: [{ type: "text", text }] };
    } catch (err) {
      return {
        content: [{ type: "text", text: `Ошибка: ${err.message}` }],
        isError: true,
      };
    }
  }
);

// ---------- Инструмент: вычисление выражения ----------
server.tool(
  "evaluate_expression",
  "Вычисляет числовое значение математического выражения: операции + - * / ^, унарный минус, скобки, sqrt(x) или √(x), константы pi и e, переменная x (по умолчанию 0).",
  {
    expression: z.string().describe("Выражение, например «sqrt(2)^2 + pi»"),
    x: z.number().optional().describe("Значение переменной x (по умолчанию 0)"),
  },
  async ({ expression, x }) => {
    try {
      const value = evaluate(expression, x ?? 0);
      const text =
        `Выражение: ${expression}${x !== undefined ? ` (при x = ${formatNumber(x)})` : ""}\n` +
        `Результат: ${formatNumber(value)}`;
      return { content: [{ type: "text", text }] };
    } catch (err) {
      return {
        content: [{ type: "text", text: `Ошибка: ${err.message}` }],
        isError: true,
      };
    }
  }
);

// ---------- Инструмент: построение графика ----------
server.tool(
  "plot_function",
  "Строит таблицу точек функции на отрезке [x_min, x_max] (по умолчанию от -10 до 10) и находит нули функции (смены знака с линейной интерполяцией). Возвращает JSON со всеми точками и нулями.",
  {
    expression: z.string().describe("Выражение функции от x, например «sin-подобное: x^2-4» — поддерживается синтаксис калькулятора"),
    x_min: z.number().optional().describe("Начало отрезка (по умолчанию -10)"),
    x_max: z.number().optional().describe("Конец отрезка (по умолчанию 10)"),
  },
  async ({ expression, x_min, x_max }) => {
    try {
      const xMin = x_min ?? -10;
      const xMax = x_max ?? 10;
      if (xMin >= xMax) throw new Error("x_min должен быть меньше x_max");
      // проверка выражения на синтаксис
      evaluate(expression, xMin);
      const points = sample(expression, xMin, xMax, 400);
      const zeroList = zeros(expression, xMin, xMax);
      const payload = {
        expression,
        x_min: xMin,
        x_max: xMax,
        points,
        zeros: zeroList.map(z => z.x),
      };
      return { content: [{ type: "text", text: JSON.stringify(payload) }] };
    } catch (err) {
      return {
        content: [{ type: "text", text: `Ошибка: ${err.message}` }],
        isError: true,
      };
    }
  }
);

// ---------- Запуск ----------
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
