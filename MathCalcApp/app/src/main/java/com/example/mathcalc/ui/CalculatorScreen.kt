package com.example.mathcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Display field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = expression.ifBlank { "0" },
                fontSize = 28.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            if (errorText != null) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (preview.isNotEmpty()) {
                Text(
                    text = preview,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // History (most recent on top, tap to reuse)
        if (history.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(history) { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expression = entry.expression }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = entry.expression,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "= " + entry.result,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f))
        }

        // Keypad
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            keypadRows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        val isEquals = key == "="
                        val isControl = key == "C" || key == "⌫"
                        Button(
                            onClick = { onKey(key) },
                            modifier = Modifier
                                .weight(if (row.size == 2 && key == "⌫") 1f else 1f)
                                .heightIn(min = 52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isEquals -> MaterialTheme.colorScheme.primary
                                    isControl -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when {
                                    isEquals -> MaterialTheme.colorScheme.onPrimary
                                    isControl -> MaterialTheme.colorScheme.onSecondaryContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        ) {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
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
