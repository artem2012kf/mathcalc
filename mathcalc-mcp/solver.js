// solver.js — чистая логика калькулятора (без MCP)

// ---------- Форматирование чисел ----------
export function formatNumber(d) {
  if (typeof d !== "number" || !isFinite(d)) return String(d);
  if (Object.is(d, -0)) d = 0;
  let s = d.toFixed(10);
  if (s.includes(".")) s = s.replace(/0+$/, "").replace(/\.$/, "");
  return s;
}

// ---------- Рекурсивный спуск: парсер выражений ----------
// Поддержка: + - * / ^, унарный минус, скобки, sqrt(...), √, √(...), числа, pi, e
export function evaluate(expr, x = 0) {
  const src = String(expr);
  let pos = 0;
  const len = src.length;

  function error(msg) {
    const e = new Error(`${msg} (позиция ${pos + 1})`);
    throw e;
  }

  function skipSpaces() {
    while (pos < len && /\s/.test(src[pos])) pos++;
  }

  function peek() {
    skipSpaces();
    return pos < len ? src[pos] : null;
  }

  function eat(ch) {
    skipSpaces();
    if (pos < len && src.startsWith(ch, pos)) {
      pos += ch.length;
      return true;
    }
    return false;
  }

  function parseNumber() {
    skipSpaces();
    const m = /^\d+(\.\d+)?|\.\d+/.exec(src.slice(pos));
    if (!m) return null;
    pos += m[0].length;
    return parseFloat(m[0]);
  }

  function parseIdentifier() {
    skipSpaces();
    const m = /^[a-zA-Zа-яА-Яπ√]+/.exec(src.slice(pos));
    if (!m) return null;
    return m[0];
  }

  function parseAtom() {
    skipSpaces();
    if (pos >= len) error("Неожиданный конец выражения");
    const c = src[pos];

    if (c === "(") {
      pos++;
      const v = parseExpr();
      if (!eat(")")) error("Ожидалась закрывающая скобка");
      return v;
    }

    // sqrt или √
    if (src.slice(pos).toLowerCase().startsWith("sqrt")) {
      pos += 4;
      skipSpaces();
      let v;
      if (eat("(")) {
        v = parseExpr();
        if (!eat(")")) error("Ожидалась закрывающая скобка");
      } else {
        v = parsePower(); // √x^2 трактуем как √(x)^2 — стандартный порядок
      }
      if (v < 0) throw new Error("Корень из отрицательного числа не определён");
      return Math.sqrt(v);
    }
    if (c === "√") {
      pos++;
      skipSpaces();
      let v;
      if (eat("(")) {
        v = parseExpr();
        if (!eat(")")) error("Ожидалась закрывающая скобка");
      } else {
        v = parsePower();
      }
      if (v < 0) throw new Error("Корень из отрицательного числа не определён");
      return Math.sqrt(v);
    }

    const num = parseNumber();
    if (num !== null) return num;

    // переменная x
    if (c === "x" || c === "X") {
      pos++;
      return x;
    }

    // константы
    const id = parseIdentifier();
    if (id) {
      const low = id.toLowerCase();
      if (low.startsWith("pi") || id === "π") {
        if (id === "π") { pos += 1; return Math.PI; }
        pos += 2;
        return Math.PI;
      }
      if (low === "e") { pos += 1; return Math.E; }
      error(`Неизвестный символ «${id}»`);
    }
    error(`Некорректный символ «${c}»`);
  }

  function parsePower() {
    let base = parseAtom();
    skipSpaces();
    if (eat("^")) {
      const exp = parseUnary(); // правая ассоциативность, допускаем унарный минус в показателе
      return Math.pow(base, exp);
    }
    return base;
  }

  function parseUnary() {
    skipSpaces();
    if (eat("-")) return -parseUnary();
    if (eat("+")) return parseUnary();
    return parsePower();
  }

  function parseTerm() {
    let v = parseUnary();
    for (;;) {
      skipSpaces();
      if (eat("*")) v *= parseUnary();
      else if (eat("/")) {
        const d = parseUnary();
        v /= d;
      }
      else break;
    }
    return v;
  }

  function parseExpr() {
    let v = parseTerm();
    for (;;) {
      skipSpaces();
      if (eat("+")) v += parseTerm();
      else if (eat("-")) v -= parseTerm();
      else break;
    }
    return v;
  }

  const result = parseExpr();
  skipSpaces();
  if (pos < len) error(`Лишний символ «${src[pos]}»`);
  return result;
}

// ---------- Квадратное уравнение ----------
function gcd(a, b) { a = Math.abs(a); b = Math.abs(b); while (b) { [a, b] = [b, a % b]; } return a; }

function isPerfectSquare(n) {
  if (n < 0 || !Number.isInteger(n)) return null;
  const r = Math.round(Math.sqrt(n));
  return r * r === n ? r : null;
}

// упростить sqrt(D): возвращает [k, rest] где D = k^2 * rest
function simplifySqrt(D) {
  if (!Number.isInteger(D)) return [1, D];
  let k = 1, rest = D;
  for (let p = 2; p * p <= rest; p++) {
    while (rest % (p * p) === 0) { rest /= p * p; k *= p; }
  }
  return [k, rest];
}

function fmtFrac(num, den) {
  const g = gcd(Math.round(num), Math.round(den));
  let n = Math.round(num) / g, d = Math.round(den) / g;
  if (d < 0) { n = -n; d = -d; }
  return d === 1 ? `${n}` : `${n}/${d}`;
}

// строка вида (p ± q√s)/d
function exactQuadraticRoot(p, q, s, d) {
  // корень = (p + q√s) / d
  const g = gcd(gcd(Math.abs(p), Math.abs(q)), Math.abs(d)) || 1;
  let P = p / g, Q = q / g, Dd = d / g;
  if (Dd < 0) { P = -P; Q = -Q; Dd = -Dd; }
  const sqrtPart = s === 1 ? `${Math.abs(Q)}` : `${Math.abs(Q) === 1 ? "" : Math.abs(Q)}√${s}`;
  if (Dd === 1) {
    if (Q === 0) return `${P}`;
    if (P === 0) return Q > 0 ? sqrtPart : `-${sqrtPart}`;
    return `${P} ${Q > 0 ? "+" : "-"} ${sqrtPart}`;
  }
  const inner = Q === 0 ? `${P}` : `${P} ${Q > 0 ? "+" : "-"} ${sqrtPart}`;
  return Q === 0 && Dd === 1 ? `${P}` : `(${inner})/${Dd}`;
}

export function solveQuadratic(a, b, c) {
  const steps = [];
  const fa = formatNumber(a), fb = formatNumber(b), fc = formatNumber(c);
  steps.push({
    title: "Стандартный вид",
    detail: `Приводим уравнение к виду ${fa}x² ${b >= 0 ? "+" : "-"} ${formatNumber(Math.abs(b))}x ${c >= 0 ? "+" : "-"} ${formatNumber(Math.abs(c))} = 0, где a = ${fa}, b = ${fb}, c = ${fc}.`
  });

  const D = b * b - 4 * a * c;
  steps.push({
    title: "Дискриминант",
    detail: `D = b² − 4ac = ${fb}² − 4·${fa}·${fc} = ${formatNumber(b * b)} − ${formatNumber(4 * a * c)} = ${formatNumber(D)}.`
  });

  let roots = [], type, summary;

  if (D > 0) {
    type = "two_roots";
    const [k, rest] = simplifySqrt(D);
    const sqD = k * Math.sqrt(rest);
    const x1 = (-b + sqD) / (2 * a);
    const x2 = (-b - sqD) / (2 * a);
    let exact1, exact2;
    if (Number.isInteger(D)) {
      exact1 = k === Math.round(Math.sqrt(D))
        ? fmtFrac(-b + k, 2 * a)
        : exactQuadraticRoot(-b, k, rest, 2 * a);
      exact2 = k === Math.round(Math.sqrt(D))
        ? fmtFrac(-b - k, 2 * a)
        : exactQuadraticRoot(-b, -k, rest, 2 * a);
    } else {
      exact1 = formatNumber(x1);
      exact2 = formatNumber(x2);
    }
    roots = [{ exact: exact1, decimal: formatNumber(x1) }, { exact: exact2, decimal: formatNumber(x2) }];
    steps.push({
      title: "D > 0 — два корня",
      detail: `√D = √${formatNumber(D)}${k > 1 || rest !== D && rest !== 1 ? ` = ${k}√${formatNumber(rest)}` : ""} = ${formatNumber(sqD)}.`
    });
    steps.push({
      title: "Формула корней",
      detail: `x₁,₂ = (−b ± √D) / (2a) = (${-b} ± ${formatNumber(sqD)}) / ${2 * a}.`
    });
    steps.push({
      title: "Вычисление",
      detail: `x₁ = ${roots[0].decimal} (точно: ${exact1}); x₂ = ${roots[1].decimal} (точно: ${exact2}).`
    });
    // проверка подстановкой
    const chk1 = a * x1 * x1 + b * x1 + c;
    const chk2 = a * x2 * x2 + b * x2 + c;
    steps.push({
      title: "Проверка подстановкой",
      detail: `a·x₁² + b·x₁ + c = ${formatNumber(chk1)} ≈ 0; a·x₂² + b·x₂ + c = ${formatNumber(chk2)} ≈ 0. Оба корня удовлетворяют уравнению.`
    });
    steps.push({
      title: "Теорема Виета",
      detail: `x₁ + x₂ = −b/a = ${formatNumber(-b / a)} (факт: ${formatNumber(x1 + x2)}); x₁·x₂ = c/a = ${formatNumber(c / a)} (факт: ${formatNumber(x1 * x2)}). Теорема Виета подтверждается.`
    });
    summary = `Уравнение имеет два корня: x₁ = ${roots[0].decimal}, x₂ = ${roots[1].decimal}.`;
  } else if (D === 0) {
    type = "one_root";
    const x = -b / (2 * a);
    roots = [{ exact: fmtFrac(-b, 2 * a), decimal: formatNumber(x) }];
    steps.push({
      title: "D = 0 — один корень",
      detail: `x = −b / (2a) = ${formatNumber(-b)} / ${formatNumber(2 * a)} = ${roots[0].decimal}.`
    });
    const chk = a * x * x + b * x + c;
    steps.push({
      title: "Проверка подстановкой",
      detail: `a·x² + b·x + c = ${formatNumber(chk)} ≈ 0. Корень верный.`
    });
    steps.push({
      title: "Теорема Виета",
      detail: `При D = 0 корень кратный: x₁ = x₂ = −b/a/2; x₁·x₂ = c/a = ${formatNumber(c / a)}.`
    });
    summary = `Уравнение имеет единственный (кратный) корень x = ${roots[0].decimal}.`;
  } else {
    type = "no_real_roots";
    roots = [];
    steps.push({
      title: "D < 0 — действительных корней нет",
      detail: `Дискриминант отрицательный (${formatNumber(D)}), поэтому квадратное уравнение не имеет действительных корней. Комплексные корни: x = (−b ± i√${formatNumber(-D)}) / (2a).`
    });
    summary = "Действительных корней нет (D < 0).";
  }

  return { type, steps, roots, summary };
}

// ---------- Линейное уравнение ----------
export function solveLinear(a, b) {
  const steps = [];
  const fa = formatNumber(a), fb = formatNumber(b);
  steps.push({
    title: "Стандартный вид",
    detail: `Уравнение имеет вид ${fa}x ${b >= 0 ? "+" : "-"} ${formatNumber(Math.abs(b))} = 0 (a = ${fa}, b = ${fb}).`
  });
  if (a === 0) {
    if (b === 0) {
      steps.push({ title: "Вырожденный случай", detail: "0 = 0 — верно при любом x." });
      return { type: "identity", steps, roots: [], summary: "Уравнение верно при любом x (бесконечно много решений)." };
    }
    steps.push({ title: "Противоречие", detail: `${fb} = 0 — неверно, корней нет.` });
    return { type: "no_solution", steps, roots: [], summary: "Корней нет (уравнение противоречиво)." };
  }
  const x = -b / a;
  steps.push({
    title: "Решение",
    detail: `x = −b / a = ${formatNumber(-b)} / ${fa} = ${formatNumber(x)}.`
  });
  const chk = a * x + b;
  steps.push({
    title: "Проверка подстановкой",
    detail: `a·x + b = ${fa}·${formatNumber(x)} ${b >= 0 ? "+" : "-"} ${formatNumber(Math.abs(b))} = ${formatNumber(chk)} ≈ 0.`
  });
  return {
    type: "linear",
    steps,
    roots: [{ exact: fmtFrac(-b, a), decimal: formatNumber(x) }],
    summary: `Линейное уравнение, один корень x = ${formatNumber(x)}.`
  };
}

// ---------- Кубическое уравнение ----------
export function solveCubic(a, b, c, d) {
  const steps = [];
  const fa = formatNumber(a), fb = formatNumber(b), fc = formatNumber(c), fd = formatNumber(d);
  steps.push({
    title: "Стандартный вид",
    detail: `Кубическое уравнение: ${fa}x³ ${b >= 0 ? "+" : "-"} ${formatNumber(Math.abs(b))}x² ${c >= 0 ? "+" : "-"} ${formatNumber(Math.abs(c))}x ${d >= 0 ? "+" : "-"} ${formatNumber(Math.abs(d))} = 0.`
  });

  // ищем рациональный корень среди делителей d / делителей a
  const constVal = Math.abs(d / a);
  const candidates = new Set();
  if (Number.isInteger(d) && Number.isInteger(a) && d !== 0) {
    const divs = [];
    for (let i = 1; i <= Math.abs(d); i++) if (d % i === 0) divs.push(i);
    const aDivs = [];
    for (let i = 1; i <= Math.abs(a); i++) if (a % i === 0) aDivs.push(i);
    for (const p of divs) for (const q of aDivs) {
      candidates.add(p / q);
      candidates.add(-p / q);
    }
  } else {
    for (let t = -20; t <= 20; t++) candidates.add(t);
    if (constVal && isFinite(constVal)) { candidates.add(constVal); candidates.add(-constVal); }
  }

  let r = null;
  for (const cand of candidates) {
    const v = a * cand * cand * cand + b * cand * cand + c * cand + d;
    if (Math.abs(v) < 1e-9) { r = cand; break; }
  }

  if (r === null) {
    // численный метод: бисекция на интервалах смены знака
    steps.push({
      title: "Рациональный корень не найден",
      detail: "Среди делителей свободного члена рационального корня нет. Применяем численный метод (деление отрезка пополам)."
    });
    const roots = [];
    const h = 0.001;
    let prevX = -50, prevY = a * prevX ** 3 + b * prevX ** 2 + c * prevX + d;
    for (let x = -50 + h; x <= 50 && roots.length < 3; x += h) {
      const y = a * x ** 3 + b * x ** 2 + c * x + d;
      if (prevY === 0) { roots.push(prevX); }
      else if (prevY * y < 0) {
        let lo = prevX, hi = x;
        for (let i = 0; i < 100; i++) {
          const mid = (lo + hi) / 2;
          const vm = a * mid ** 3 + b * mid ** 2 + c * mid + d;
          if ((a * lo ** 3 + b * lo ** 2 + c * lo + d) * vm <= 0) hi = mid; else lo = mid;
        }
        roots.push((lo + hi) / 2);
      }
      prevX = x; prevY = y;
    }
    const uniq = [];
    for (const rr of roots) if (!uniq.some(u => Math.abs(u - rr) < 1e-6)) uniq.push(rr);
    for (const rr of uniq) {
      steps.push({
        title: "Численный корень",
        detail: `Методом бисекции найден корень x ≈ ${formatNumber(rr)}.`
      });
    }
    return {
      type: "cubic_numeric",
      steps,
      roots: uniq.map(rr => ({ exact: `≈ ${formatNumber(rr)}`, decimal: formatNumber(rr) })),
      summary: `Рациональных корней нет; численно (бисекция) найдены корни: ${uniq.map(formatNumber).join(", ")}. (Примечание: использован численный метод.)`
    };
  }

  steps.push({
    title: "Рациональный корень",
    detail: `Найден рациональный корень x = ${formatNumber(r)} (проверка: a·r³ + b·r² + c·r + d = 0).`
  });

  // делим на (x − r): ax³+bx²+cx+d = (x−r)(Ax²+Bx+C)
  const A = a;
  const B = b + A * r;
  const C = c + B * r;
  steps.push({
    title: "Разложение на множители",
    detail: `Делим на (x − ${formatNumber(r)}): получаем (x − ${formatNumber(r)})(${formatNumber(A)}x² ${B >= 0 ? "+" : "-"} ${formatNumber(Math.abs(B))}x ${C >= 0 ? "+" : "-"} ${formatNumber(Math.abs(C))}) = 0.`
  });

  const allRoots = [{ exact: fmtFrac(1, 1) === "1" ? formatNumber(r) : formatNumber(r), decimal: formatNumber(r) }];
  allRoots[0].exact = formatNumber(r);

  if (A === 0) {
    steps.push({ title: "Итог", detail: `Единственный корень x = ${formatNumber(r)}.` });
    return { type: "cubic_linear_factor", steps, roots: allRoots, summary: `Корень x = ${formatNumber(r)}.` };
  }

  const quad = solveQuadratic(A, B, C);
  steps.push({
    title: "Оставшееся квадратное уравнение",
    detail: `Решаем ${formatNumber(A)}x² ${B >= 0 ? "+" : "-"} ${formatNumber(Math.abs(B))}x ${C >= 0 ? "+" : "-"} ${formatNumber(Math.abs(C))} = 0.`
  });
  steps.push(...quad.steps.map(s => ({ title: `[Квадратный множитель] ${s.title}`, detail: s.detail })));

  const roots = [...allRoots, ...quad.roots];
  const rootStrs = roots.map(rt => rt.decimal).join(", ");
  steps.push({
    title: "Проверка (теорема Виета для кубики)",
    detail: `Сумма корней = −b/a = ${formatNumber(-b / a)} (факт: ${formatNumber(roots.reduce((s, rt) => s + parseFloat(rt.decimal), 0))}).`
  });

  return {
    type: quad.type === "no_real_roots" ? "cubic_one_real_root" : "cubic",
    steps,
    roots,
    summary: quad.type === "no_real_roots"
      ? `Один действительный корень x = ${formatNumber(r)}; остальные — комплексные.`
      : `Действительные корни: ${rootStrs}.`
  };
}

// ---------- Диспетчер ----------
export function solve(kind, coefs) {
  if (kind === "linear") return solveLinear(coefs[0], coefs[1]);
  if (kind === "quadratic") return solveQuadratic(coefs[0], coefs[1], coefs[2]);
  if (kind === "cubic") return solveCubic(coefs[0], coefs[1], coefs[2], coefs[3]);
  throw new Error(`Неизвестный тип уравнения: ${kind}`);
}

// ---------- Графика: сэмплирование и нули ----------
export function sample(expr, xMin, xMax, points = 400) {
  const out = [];
  const step = (xMax - xMin) / points;
  let prevY = null;
  for (let i = 0; i <= points; i++) {
    const x = xMin + i * step;
    let y;
    try { y = evaluate(expr, x); } catch { y = null; }
    if (y !== null && (Number.isNaN(y) || !isFinite(y))) y = null;
    // разрыв: соседние точки отличаются слишком сильно и знак меняется
    if (y !== null && prevY !== null && Math.abs(y - prevY) > 1e6) y = null;
    out.push({ x: formatNumber(x), y: y === null ? null : formatNumber(y) });
    prevY = y;
  }
  return out;
}

export function zeros(expr, xMin, xMax) {
  const res = [];
  const points = 2000;
  const step = (xMax - xMin) / points;
  let prevX = xMin, prevY;
  try { prevY = evaluate(expr, prevX); } catch { prevY = null; }
  if (prevY !== null && (Number.isNaN(prevY) || !isFinite(prevY))) prevY = null;
  for (let i = 1; i <= points; i++) {
    const x = xMin + i * step;
    let y;
    try { y = evaluate(expr, x); } catch { y = null; }
    if (y !== null && (Number.isNaN(y) || !isFinite(y))) y = null;
    if (prevY !== null && y !== null) {
      if (prevY === 0) {
        res.push({ x: formatNumber(prevX) });
      } else if (prevY * y < 0) {
        const z = prevX + (x - prevX) * Math.abs(prevY) / (Math.abs(prevY) + Math.abs(y));
        res.push({ x: formatNumber(z) });
      }
    }
    prevX = x; prevY = y;
  }
  return res;
}
