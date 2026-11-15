package com.example.mathcalc.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathcalc.math.PlotData
import com.example.mathcalc.settings.Loc
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val CanvasBg = Color(0xFF12141F)
private val ZeroRed = Color(0xFFFF5A6E)

private fun niceStep(range: Double): Double {
    if (range <= 0.0) return 1.0
    val raw = range / 10.0
    val mag = 10.0.pow(floor(log10(raw)))
    val norm = raw / mag
    val nice = when {
        norm < 1.5 -> 1.0
        norm < 3.0 -> 2.0
        norm < 7.0 -> 5.0
        else -> 10.0
    }
    return nice * mag
}

private fun parseDouble(s: String, fallback: Double): Double {
    return s.replace(',', '.').toDoubleOrNull() ?: fallback
}

@Composable
fun GraphScreen(modifier: Modifier = Modifier) {
    var expr by remember { mutableStateOf("x^2-5x+6") }
    var xMinText by remember { mutableStateOf("-10") }
    var xMaxText by remember { mutableStateOf("10") }

    var xMin by remember { mutableDoubleStateOf(-10.0) }
    var xMax by remember { mutableDoubleStateOf(10.0) }
    var appliedExpr by remember { mutableStateOf("x^2-5x+6") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Loc["graphTitle"],
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ---------- Formula bar card ----------
        TextField(
            value = expr,
            onValueChange = { expr = it },
            label = { Text(Loc["functionLabel"]) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Indigo,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Teal,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextField(
                value = xMinText,
                onValueChange = { xMinText = it },
                label = { Text(Loc["xMin"]) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Indigo,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Teal,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = xMaxText,
                onValueChange = { xMaxText = it },
                label = { Text(Loc["xMax"]) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Indigo,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Teal,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .height(52.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Indigo, Violet)),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        val newMin = parseDouble(xMinText, xMin)
                        val newMax = parseDouble(xMaxText, xMax)
                        if (newMin < newMax) {
                            xMin = newMin
                            xMax = newMax
                        }
                        appliedExpr = expr
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Loc["plot"],
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        // ---------- Canvas card ----------
        GraphCanvas(
            expr = appliedExpr,
            xMin = xMin,
            xMax = xMax,
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            onPan = { dx ->
                val shift = dx * (xMax - xMin)
                xMin -= shift
                xMax -= shift
                xMinText = formatAxisNum(xMin)
                xMaxText = formatAxisNum(xMax)
            }
        )

        // ---------- Zoom controls: floating pill buttons ----------
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ZoomPill(label = "+", contentDesc = Loc["zoomIn"]) {
                val mid = (xMin + xMax) / 2.0
                val half = (xMax - xMin) / 2.0 * 0.5
                xMin = mid - half
                xMax = mid + half
                xMinText = formatAxisNum(xMin)
                xMaxText = formatAxisNum(xMax)
            }
            ZoomPill(label = "−", contentDesc = Loc["zoomOut"]) {
                val mid = (xMin + xMax) / 2.0
                val half = (xMax - xMin) / 2.0 * 2.0
                xMin = mid - half
                xMax = mid + half
                xMinText = formatAxisNum(xMin)
                xMaxText = formatAxisNum(xMax)
            }
            ZoomPill(label = Loc["reset"], contentDesc = Loc["resetZoom"]) {
                xMin = -10.0
                xMax = 10.0
                xMinText = "-10"
                xMaxText = "10"
            }
        }

        ZerosInfo(expr = appliedExpr, xMin = xMin, xMax = xMax)
    }
}

@Composable
private fun ZoomPill(
    label: String,
    contentDesc: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

private fun formatAxisNum(v: Double): String {
    val r = Math.round(v * 1e4) / 1e4
    return if (r == floor(r)) r.toLong().toString() else r.toString()
}

@Composable
private fun GraphCanvas(
    expr: String,
    xMin: Double,
    xMax: Double,
    modifier: Modifier = Modifier,
    onPan: (Double) -> Unit
) {
    val points = remember(expr, xMin, xMax) {
        try {
            PlotData.sample(expr, xMin, xMax)
        } catch (e: Exception) {
            emptyList()
        }
    }
    val zeros = remember(expr, xMin, xMax) {
        try {
            PlotData.zeros(expr, xMin, xMax)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Vertical range: derive from sampled y values with padding
    val yMinYMax = remember(points, xMin, xMax) {
        val ys = points.mapNotNull { it.second }.filter { it.isFinite() }
        if (ys.isEmpty()) {
            -10.0 to 10.0
        } else {
            var lo = ys.min()
            var hi = ys.max()
            if (hi - lo < 1e-6) { lo -= 1.0; hi += 1.0 }
            val pad = (hi - lo) * 0.12
            (lo - pad) to (hi + pad)
        }
    }
    val yMin = yMinYMax.first
    val yMax = yMinYMax.second

    val labelColor = TextMuted
    val gridColor = Color(0xFF2A2D42)
    val axisColor = Color(0xFF565A78)
    val curveColor = Indigo
    val zeroColor = ZeroRed
    val density = LocalDensity.current
    val labelPx = with(density) { 11.sp.toPx() }
    val labelPaint = remember(labelColor) {
        android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (labelColor.alpha * 255).toInt(),
                (labelColor.red * 255).toInt(),
                (labelColor.green * 255).toInt(),
                (labelColor.blue * 255).toInt()
            )
            textSize = labelPx
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
    }

    Box(
        modifier = modifier
            .background(CanvasBg, RoundedCornerShape(20.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .pointerInput(xMin, xMax) {
                    var totalDrag = 0f
                    var lastX = 0f
                    detectDragGestures(
                        onDragStart = { totalDrag = 0f; lastX = it.x },
                        onDrag = { change, dragAmount ->
                            onPan(-(dragAmount.x / size.width.toDouble()))
                            change.consume()
                        }
                    )
                }
        ) {
            val w = size.width
            val h = size.height
            fun xToPx(x: Double): Float = ((x - xMin) / (xMax - xMin) * w).toFloat()
            fun yToPx(y: Double): Float = ((yMax - y) / (yMax - yMin) * h).toFloat()

            // Grid + tick labels (subtle)
            val step = niceStep(xMax - xMin)
            val first = ceil(xMin / step) * step
            var tick = first
            while (tick <= xMax + 1e-9) {
                val px = xToPx(tick)
                drawLine(gridColor, Offset(px, 0f), Offset(px, h), 1f)
                if (abs(tick) > step / 1000) {
                    drawContext.canvas.nativeCanvas.drawText(
                        formatAxisNum(Math.round(tick * 1e6) / 1e6),
                        px,
                        (yToPx(0.0).coerceIn(labelPx + 2f, h - 4f)) + labelPx,
                        labelPaint
                    )
                }
                tick += step
            }

            // Horizontal grid lines (using same nice step scaled to y-range)
            val yStep = niceStep(yMax - yMin)
            var yTick = ceil(yMin / yStep) * yStep
            while (yTick <= yMax + 1e-9) {
                val py = yToPx(yTick)
                drawLine(gridColor, Offset(0f, py), Offset(w, py), 1f)
                yTick += yStep
            }

            // Axes
            val y0px = yToPx(0.0)
            if (y0px in 0f..h) {
                drawLine(axisColor, Offset(0f, y0px), Offset(w, y0px), 2f)
                // arrowhead
                drawLine(axisColor, Offset(w - 12f, y0px - 5f), Offset(w, y0px), 2f)
                drawLine(axisColor, Offset(w - 12f, y0px + 5f), Offset(w, y0px), 2f)
            }
            val x0px = xToPx(0.0)
            if (x0px in 0f..w) {
                drawLine(axisColor, Offset(x0px, 0f), Offset(x0px, h), 2f)
                drawLine(axisColor, Offset(x0px - 5f, 12f), Offset(x0px, 0f), 2f)
                drawLine(axisColor, Offset(x0px + 5f, 12f), Offset(x0px, 0f), 2f)
            }

            // Curve: glowing — wide translucent halo pass, then crisp main stroke.
            // Segments are broken at discontinuities (null y).
            val haloStyle = Stroke(
                width = 12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            val mainStyle = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
            fun drawSegments(color: Color, style: Stroke) {
                var segment = Path()
                var started = false
                points.forEach { (x, y) ->
                    if (y == null || !y.isFinite()) {
                        if (started) drawPath(segment, color, style = style)
                        segment = Path()
                        started = false
                    } else {
                        val px = xToPx(x)
                        val py = yToPx(y)
                        if (!started) {
                            segment.moveTo(px, py)
                            started = true
                        } else {
                            segment.lineTo(px, py)
                        }
                    }
                }
                if (started) drawPath(segment, color, style = style)
            }
            drawSegments(curveColor.copy(alpha = 0.22f), haloStyle)
            drawSegments(curveColor, mainStyle)

            // Zeros: glowing red dots with labels
            zeros.forEach { z ->
                val px = xToPx(z)
                val py = yToPx(0.0)
                if (py in 0f..h && px in 0f..w) {
                    drawCircle(zeroColor.copy(alpha = 0.25f), radius = 13f, center = Offset(px, py))
                    drawCircle(zeroColor.copy(alpha = 0.45f), radius = 9f, center = Offset(px, py))
                    drawCircle(zeroColor, radius = 5f, center = Offset(px, py))
                    drawContext.canvas.nativeCanvas.drawText(
                        formatAxisNum(Math.round(z * 1e4) / 1e4),
                        px,
                        py - 12f,
                        labelPaint
                    )
                }
            }
        }
    }
}

@Composable
private fun ZerosInfo(expr: String, xMin: Double, xMax: Double) {
    val zeros = remember(expr, xMin, xMax) {
        try {
            PlotData.zeros(expr, xMin, xMax)
        } catch (e: Exception) {
            emptyList()
        }
    }
    if (zeros.isEmpty()) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = Loc["zerosOn"].format(formatAxisNum(xMin), formatAxisNum(xMax)),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = zeros.joinToString(", ") { formatAxisNum(Math.round(it * 1e4) / 1e4) },
                fontSize = 16.sp,
                color = Teal,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
