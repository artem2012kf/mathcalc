package com.example.mathcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathcalc.math.EquationResult
import com.example.mathcalc.math.EquationSolver
import com.example.mathcalc.settings.Loc

private enum class EqType {
    LINEAR, QUADRATIC, CUBIC
}

@Composable
private fun coeffField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { newText ->
            // allow digits, minus and one decimal separator (both , and .)
            if (newText.isEmpty() || Regex("^-?\\d*([.,]\\d*)?$").matches(newText)) {
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

private fun parseCoeff(s: String): Double {
    return s.replace(',', '.').toDoubleOrNull() ?: 0.0
}

@Composable
fun EquationScreen(modifier: Modifier = Modifier) {
    var selected by remember { mutableStateOf(EqType.LINEAR) }
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("-2") }
    var c by remember { mutableStateOf("0") }
    var d by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun solve() {
        result = null
        error = null
        try {
            result = when (selected) {
                EqType.LINEAR -> EquationSolver.solveLinear(parseCoeff(a), parseCoeff(b))
                EqType.QUADRATIC -> EquationSolver.solveQuadratic(parseCoeff(a), parseCoeff(b), parseCoeff(c))
                EqType.CUBIC -> EquationSolver.solveCubic(parseCoeff(a), parseCoeff(b), parseCoeff(c), parseCoeff(d))
            }
        } catch (e: Exception) {
            error = Loc["solveFailed"].let { fmt ->
                fmt.format(e.message ?: Loc["unknownError"])
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Loc["solveEquationsTitle"],
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Type selector: pill chips via styled segmented buttons
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
            space = 0.dp
        ) {
            EqType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selected == type,
                    onClick = { selected = type },
                    shape = SegmentedButtonDefaults.itemShape(index, EqType.entries.size),
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
                            text = when (type) {
                                EqType.LINEAR -> Loc["eqLinearShort"]
                                EqType.QUADRATIC -> Loc["eqQuadraticShort"]
                                EqType.CUBIC -> Loc["eqCubicShort"]
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }

        // Full type name as subtitle
        Text(
            text = when (selected) {
                EqType.LINEAR -> Loc["eqLinearFull"]
                EqType.QUADRATIC -> Loc["eqQuadraticFull"]
                EqType.CUBIC -> Loc["eqCubicFull"]
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Coefficient fields
        when (selected) {
            EqType.LINEAR -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    coeffField("a", a, { a = it }, Modifier.weight(1f))
                    coeffField("b", b, { b = it }, Modifier.weight(1f))
                }
            }
            EqType.QUADRATIC -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    coeffField("a", a, { a = it }, Modifier.weight(1f))
                    coeffField("b", b, { b = it }, Modifier.weight(1f))
                    coeffField("c", c, { c = it }, Modifier.weight(1f))
                }
            }
            EqType.CUBIC -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    coeffField("a", a, { a = it }, Modifier.weight(1f))
                    coeffField("b", b, { b = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    coeffField("c", c, { c = it }, Modifier.weight(1f))
                    coeffField("d", d, { d = it }, Modifier.weight(1f))
                }
            }
        }

        // Solve button: full-width, tall, vertical gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(Indigo, Violet)),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { solve() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Loc["solve"],
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        // Summary card
        result?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = res.type,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = res.summary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Roots highlighted gradient card
            if (res.roots.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    NavySurfaceHigh
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = Loc["roots"],
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Teal
                        )
                        res.roots.forEachIndexed { i, r ->
                            Text(
                                text = "x${i + 1} = ${formatRoot(r)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Steps as a vertical timeline
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                res.steps.forEachIndexed { index, step ->
                    StepTimelineItem(
                        title = step.title,
                        detail = step.detail,
                        isLast = index == res.steps.lastIndex,
                        accent = if (index % 2 == 0) Indigo else Teal
                    )
                }
            }
        }
    }
}

@Composable
private fun StepTimelineItem(
    title: String,
    detail: String,
    isLast: Boolean,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline rail: colored dot + vertical connecting line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(accent, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(accent.copy(alpha = 0.35f))
                )
            }
        }

        // Step content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                )
            }
        }
    }
}

private fun formatRoot(r: Double): String {
    val rounded = Math.round(r * 1e6) / 1e6
    return if (rounded == Math.floor(rounded) && !rounded.isInfinite()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
