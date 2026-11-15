package com.example.mathcalc.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathcalc.math.Calculator

private data class HistoryEntry(val expression: String, val result: String)

private val keypadRows = listOf(
    listOf("7", "8", "9", "÷"),
    listOf("4", "5", "6", "×"),
    listOf("1", "2", "3", "−"),
    listOf("0", ".", "√", "+"),
    listOf("(", ")", "^", "="),
    listOf("C", "⌫")
)

private val operators = setOf("÷", "×", "−", "+", "^")

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<HistoryEntry>() }

    val preview: String = remember(expression) {
        if (expression.isBlank()) ""
        else try {
            "=" + Calculator.formatNumber(Calculator.evaluate(expression))
        } catch (e: Exception) {
            ""
        }
    }
    val errorText: String? = remember(expression) {
        if (expression.isBlank()) null
        else try {
            Calculator.evaluate(expression)
            null
        } catch (e: Exception) {
            "Ошибка в выражении"
        }
    }

    fun onKey(key: String) {
        when (key) {
            "C" -> expression = ""
            "⌫" -> if (expression.isNotEmpty()) expression = expression.dropLast(1)
            "=" -> {
                if (expression.isBlank()) return
                val result = try {
                    Calculator.formatNumber(Calculator.evaluate(expression))
                } catch (e: Exception) {
                    return // error already shown in red text, do not add to history
                }
                history.add(0, HistoryEntry(expression, result))
                expression = result
            }
            else -> expression += key
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- Display: gradient card ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Indigo, Violet)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = expression.ifBlank { "0" },
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorText != null) {
                    Text(
                        text = errorText,
                        color = Color(0xFFFFD4DB),
                        fontSize = 13.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                } else if (preview.isNotEmpty()) {
                    Text(
                        text = preview,
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC5F2E9), // light teal
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
            }
        }

        // ---------- History ----------
        if (history.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "История",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { expression = entry.expression }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = entry.expression,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "= " + entry.result,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Teal,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        // ---------- Keypad ----------
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keypadRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        CalcKey(
                            label = key,
                            onClick = { onKey(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space for the 2-button row (C ⌫)
                    if (row.size == 2) {
                        Box(modifier = Modifier.weight(2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEquals = label == "="
    val isClear = label == "C"
    val isBackspace = label == "⌫"
    val isOperator = label in operators

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 90),
        label = "keyScale"
    )

    val shape = if (isEquals) RoundedCornerShape(24.dp) else RoundedCornerShape(32.dp)
    val containerColor = when {
        isEquals -> Indigo
        isClear -> MaterialTheme.colorScheme.errorContainer
        isBackspace -> MaterialTheme.colorScheme.surfaceVariant
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isEquals -> Color.White
        isClear -> MaterialTheme.colorScheme.onErrorContainer
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(modifier = modifier) {
        Button(
            onClick = onClick,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp,
                pressedElevation = 0.dp
            )
        ) {
            Text(
                text = label,
                fontSize = if (isEquals) 24.sp else 20.sp,
                fontWeight = if (isOperator || isEquals || isClear) FontWeight.SemiBold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
