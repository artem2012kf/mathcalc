package com.example.mathcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathcalc.math.EquationResult
import com.example.mathcalc.math.Examples
import com.example.mathcalc.math.Fraction
import com.example.mathcalc.math.FractionSolver
import com.example.mathcalc.settings.Loc

private enum class ExamplesMode { FRACTIONS, PERCENT, DIVISIBILITY }

@Composable
private fun numField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowFraction: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = { newText ->
            val pattern = if (allowFraction) "^-?\\d*([.,]\\d*)?$" else "^-?\\d*$"
            if (newText.isEmpty() || Regex(pattern).matches(newText)) {
                onValueChange(newText)
            }
        },
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
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
        modifier = modifier
    )
}

@Composable
private fun gradientButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(
                brush = Brush.verticalGradient(listOf(Indigo, Violet)),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun resultBlock(result: EquationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = result.type,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = result.summary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        result.steps.forEachIndexed { index, step ->
            StepTimelineItem(
                title = step.title,
                detail = step.detail,
                isLast = index == result.steps.lastIndex,
                accent = if (index % 2 == 0) Indigo else Teal
            )
        }
    }
}

private fun parseLong(s: String): Long = s.toLongOrNull() ?: 0L

private fun parseDouble(s: String): Double = s.replace(',', '.').toDoubleOrNull() ?: 0.0

@Composable
fun ExamplesScreen(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(ExamplesMode.FRACTIONS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Loc["navExamples"],
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
            space = 0.dp
        ) {
            ExamplesMode.entries.forEachIndexed { index, m ->
                SegmentedButton(
                    selected = mode == m,
                    onClick = { mode = m },
                    shape = SegmentedButtonDefaults.itemShape(index, ExamplesMode.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = Color.White,
                        activeBorderColor = Color.Transparent,
                        inactiveContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        inactiveBorderColor = Color.Transparent
                    ),
                    label = {
                        Text(
                            text = when (m) {
                                ExamplesMode.FRACTIONS -> Loc["modeFractions"]
                                ExamplesMode.PERCENT -> Loc["modePercent"]
                                ExamplesMode.DIVISIBILITY -> Loc["modeDivisibility"]
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }

        when (mode) {
            ExamplesMode.FRACTIONS -> FractionsMode()
            ExamplesMode.PERCENT -> PercentMode()
            ExamplesMode.DIVISIBILITY -> DivisibilityMode()
        }
    }
}

@Composable
private fun FractionsMode() {
    var n1 by remember { mutableStateOf("1") }
    var d1 by remember { mutableStateOf("2") }
    var n2 by remember { mutableStateOf("1") }
    var d2 by remember { mutableStateOf("3") }
    var op by remember { mutableStateOf('+') }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun compute() {
        result = null
        error = null
        try {
            val den1 = parseLong(d1)
            val den2 = parseLong(d2)
            require(den1 != 0L && den2 != 0L) { Loc["denominator"] + " ≠ 0" }
            result = FractionSolver.solveFractionExample(
                Fraction(parseLong(n1), den1),
                Fraction(parseLong(n2), den2),
                op
            )
        } catch (e: Exception) {
            error = e.message ?: Loc["unknownError"]
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        numField(Loc["numerator"], n1, { n1 = it }, Modifier.weight(1f))
        Text("/", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        numField(Loc["denominator"], d1, { d1 = it }, Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        numField(Loc["numerator"], n2, { n2 = it }, Modifier.weight(1f))
        Text("/", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        numField(Loc["denominator"], d2, { d2 = it }, Modifier.weight(1f))
    }

    // Operation selector: small pills
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf('+', '−', '×', '÷').forEach { symbol ->
            val opChar = when (symbol) {
                '−' -> '-'
                '×' -> '*'
                '÷' -> '/'
                else -> '+'
            }
            val selected = op == opChar
            Box(
                modifier = Modifier
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { op = opChar }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = symbol.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    gradientButton(Loc["compute"]) { compute() }

    error?.let { Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    result?.let { resultBlock(it) }
}

@Composable
private fun PercentMode() {
    var x by remember { mutableStateOf("250") }
    var p by remember { mutableStateOf("15") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun run(compute: () -> EquationResult) {
        result = null
        error = null
        try {
            result = compute()
        } catch (e: Exception) {
            error = e.message ?: Loc["unknownError"]
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        numField(Loc["percentNumber"], x, { x = it }, Modifier.weight(1f))
        numField(Loc["percentPercent"], p, { p = it }, Modifier.weight(1f))
    }

    gradientButton(Loc["percentOfBtn"]) {
        run { Examples.percentageOf(parseDouble(x), parseDouble(p)) }
    }
    gradientButton(Loc["percentIsWhatBtn"]) {
        run { Examples.percentOfWhat(parseDouble(x), parseDouble(p)) }
    }

    error?.let { Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    result?.let { resultBlock(it) }
}

@Composable
private fun DivisibilityMode() {
    var a by remember { mutableStateOf("12") }
    var b by remember { mutableStateOf("8") }
    var n by remember { mutableStateOf("360") }
    var base by remember { mutableStateOf("2") }
    var exp by remember { mutableStateOf("10") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun run(compute: () -> EquationResult) {
        result = null
        error = null
        try {
            result = compute()
        } catch (e: Exception) {
            error = e.message ?: Loc["unknownError"]
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        numField(Loc["numberA"], a, { a = it }, Modifier.weight(1f))
        numField(Loc["numberB"], b, { b = it }, Modifier.weight(1f))
    }
    gradientButton(Loc["gcdLcmBtn"]) {
        run { Examples.gcdLcm(parseLong(a), parseLong(b)) }
    }

    numField(Loc["modeDivisibility"], n, { n = it }, Modifier.fillMaxWidth())
    gradientButton(Loc["factorBtn"]) {
        run { Examples.primeFactorization(parseLong(n)) }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        numField(Loc["baseLabel"], base, { base = it }, Modifier.weight(1f))
        numField(Loc["expLabel"], exp, { exp = it }, Modifier.weight(1f))
    }
    gradientButton(Loc["powerBtn"]) {
        run { Examples.powerOf(parseLong(base), parseLong(exp).toInt()) }
    }

    error?.let { Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp) }
    result?.let { resultBlock(it) }
}
