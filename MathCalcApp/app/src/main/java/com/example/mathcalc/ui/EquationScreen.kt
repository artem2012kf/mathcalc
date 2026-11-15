package com.example.mathcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathcalc.math.EquationResult
import com.example.mathcalc.math.EquationSolver

private enum class EqType(val label: String) {
    LINEAR("Линейное ax+b=0"),
    QUADRATIC("Квадратное ax²+bx+c=0"),
    CUBIC("Кубическое ax³+bx²+cx+d=0")
}

@Composable
private fun coeffField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newText ->
            // allow digits, minus and one decimal separator (both , and .)
            if (newText.isEmpty() || Regex("^-?\\d*([.,]\\d*)?$").matches(newText)) {
                onValueChange(newText)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
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
            error = "Не удалось решить уравнение: ${e.message ?: "неизвестная ошибка"}"
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
            text = "Решение уравнений",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Type selector
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            EqType.entries.forEachIndexed { index, type ->
                SegmentedButton(
                    selected = selected == type,
                    onClick = { selected = type },
                    shape = SegmentedButtonDefaults.itemShape(index, EqType.entries.size),
                    label = {
                        Text(
                            text = when (type) {
                                EqType.LINEAR -> "Линейное"
                                EqType.QUADRATIC -> "Квадратное"
                                EqType.CUBIC -> "Кубическое"
                            },
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        // Full type name as subtitle
        Text(
            text = selected.label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Coefficient fields
        when (selected) {
            EqType.LINEAR -> {
                coeffField("a", a, { a = it })
                coeffField("b", b, { b = it })
            }
            EqType.QUADRATIC -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    coeffField("a", a, { a = it }, Modifier.weight(1f))
                    coeffField("b", b, { b = it }, Modifier.weight(1f))
                    coeffField("c", c, { c = it }, Modifier.weight(1f))
                }
            }
            EqType.CUBIC -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    coeffField("a", a, { a = it }, Modifier.weight(1f))
                    coeffField("b", b, { b = it }, Modifier.weight(1f))
                    coeffField("c", c, { c = it }, Modifier.weight(1f))
                    coeffField("d", d, { d = it }, Modifier.weight(1f))
                }
            }
        }

        Button(
            onClick = { solve() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Решить")
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = res.type,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = res.summary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Roots highlighted card
            if (res.roots.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = RootGreen.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Корни:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        res.roots.forEachIndexed { i, r ->
                            Text(
                                text = "x${i + 1} = ${formatRoot(r)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RootGreen,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Step cards
            res.steps.forEach { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = step.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = step.detail,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
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
