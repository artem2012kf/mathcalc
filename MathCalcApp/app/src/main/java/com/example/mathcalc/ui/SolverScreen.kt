package com.example.mathcalc.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.mathcalc.math.EquationResult
import com.example.mathcalc.math.EquationSolver
import com.example.mathcalc.math.PhotoEquationParser
import com.example.mathcalc.math.SmartSolver
import com.example.mathcalc.settings.Loc
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val ScanGreen = Color(0xFF3DDC84)

@Composable
fun SolverScreen(modifier: Modifier = Modifier) {
    var tab by remember { mutableStateOf(0) } // 0 = Photo, 1 = Input

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Loc["solveTitle"],
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Photo / Input tab switch
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
            space = 0.dp
        ) {
            val tabs = listOf(Loc["photo"], Loc["tabInput"])
            tabs.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = tab == index,
                    onClick = { tab = index },
                    shape = SegmentedButtonDefaults.itemShape(index, tabs.size),
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
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }

        when (tab) {
            0 -> PhotoTab()
            else -> InputTab()
        }
    }
}

// =====================================================================
// TAB 1: Photo — live camera scanning + gallery fallback
// =====================================================================

@Composable
private fun PhotoTab() {
    var scanning by remember { mutableStateOf(true) }
    var detectedEquation by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    fun recognizePhoto(uri: android.net.Uri) {
        detectedEquation = null
        error = null
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val parsed = PhotoEquationParser.parseEquation(visionText.text)
                    if (parsed == null) {
                        error = Loc["photoError"]
                    } else {
                        detectedEquation = parsed.third
                        try {
                            result = EquationSolver.solvePolynomial(parsed.second)
                        } catch (e: Exception) {
                            error = Loc["solveFailed"].let { it.format(e.message ?: Loc["unknownError"]) }
                        }
                    }
                }
                .addOnFailureListener { error = Loc["photoError"] }
        } catch (e: Exception) {
            error = Loc["photoError"]
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scanning = false
            recognizePhoto(uri)
        }
    }

    // Live camera viewfinder with dark scrim + scan frame
    CameraSection(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        scanning = scanning && detectedEquation == null,
        detected = detectedEquation != null,
        onDetected = { raw ->
            if (detectedEquation == null) {
                val parsed = PhotoEquationParser.parseEquation(raw)
                if (parsed != null) {
                    detectedEquation = parsed.third
                    try {
                        result = EquationSolver.solvePolynomial(parsed.second)
                        error = null
                    } catch (e: Exception) {
                        error = Loc["solveFailed"].let { it.format(e.message ?: Loc["unknownError"]) }
                    }
                }
            }
        }
    )

    // Status line under the viewfinder
    when {
        detectedEquation != null -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("✓", color = ScanGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = Loc["detectedOk"],
                    color = ScanGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        else -> Text(
            text = Loc["scanning"],
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Detected equation card
    detectedEquation?.let { eq ->
        Text(
            text = Loc["photoSource"].format(eq),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(10.dp)
        )
    }

    // Buttons: scan again / gallery
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (detectedEquation != null) {
            Button(
                onClick = {
                    detectedEquation = null
                    result = null
                    error = null
                    scanning = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = ScanGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = Loc["scanAgain"], fontWeight = FontWeight.SemiBold)
            }
        }
        Button(
            onClick = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier.weight(1f)
        ) {
            Text(text = Loc["fromGallery"], fontWeight = FontWeight.SemiBold)
        }
    }

    error?.let {
        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
    }

    ResultSection(result)
}

/**
 * Live camera preview with ML Kit text recognition on frames (throttled ~700ms).
 * Handles permission flow and bind failures gracefully.
 */
@Composable
private fun CameraSection(
    modifier: Modifier = Modifier,
    scanning: Boolean,
    detected: Boolean,
    onDetected: (String) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    if (!hasPermission) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Loc["cameraPermissionTitle"],
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Loc["cameraPermissionText"],
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    hasPermission = it
                }
                Button(
                    onClick = { permLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.padding(top = 12.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(text = Loc["grantCamera"])
                }
            }
        }
        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val active = remember { AtomicBoolean(true) }
    LaunchedEffect(scanning) { active.set(scanning) }

    val executor = remember { Executors.newSingleThreadExecutor() }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var provider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    DisposableEffect(Unit) {
        onDispose {
            provider?.unbindAll()
            executor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    try {
                        val future = ProcessCameraProvider.getInstance(ctx)
                        future.addListener({
                            try {
                                val p = future.get()
                                provider = p
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                analysis.setAnalyzer(executor, EquationAnalyzer(active, onDetected))
                                p.unbindAll()
                                p.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysis
                                )
                            } catch (e: Exception) {
                                cameraError = Loc["cameraError"].format(e.message ?: Loc["unknownError"])
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    } catch (e: Exception) {
                        cameraError = Loc["cameraError"].format(e.message ?: Loc["unknownError"])
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Dark scrim outside the scan frame + colored frame border + scanning line
        val progress = if (scanning) scanLineProgress() else 0f
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
            val frameW = size.width * 0.82f
            val frameH = size.height * 0.42f
            val topLeft = Offset((size.width - frameW) / 2f, (size.height - frameH) / 2f)
            // scrim with a punched-out rounded window
            drawRect(color = Color.Black.copy(alpha = 0.55f))
            drawRoundRect(
                color = Color.Transparent,
                topLeft = topLeft,
                size = Size(frameW, frameH),
                cornerRadius = CornerRadius(28f, 28f),
                blendMode = BlendMode.Clear
            )
            // frame border: green when detected, white while scanning
            drawRoundRect(
                color = if (detected) ScanGreen else Color.White.copy(alpha = 0.9f),
                topLeft = topLeft,
                size = Size(frameW, frameH),
                cornerRadius = CornerRadius(28f, 28f),
                style = Stroke(width = if (detected) 8f else 5f)
            )
            if (scanning) {
                val lineY = topLeft.y + frameH * progress
                drawLine(
                    color = ScanGreen,
                    start = Offset(topLeft.x + 20f, lineY),
                    end = Offset(topLeft.x + frameW - 20f, lineY),
                    strokeWidth = 6f
                )
            }
        }
    }

    cameraError?.let {
        Text(
            text = it,
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Animated 0..1 progress of the scanning line. */
@Composable
private fun scanLineProgress(): Float {
    val transition = rememberInfiniteTransition(label = "scan")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )
    return p
}

/**
 * Frame analyzer: runs ML Kit text recognition at most every ~700ms,
 * only while [active] is true. Calls [onDetected] with the raw recognized
 * text of the first frame that parses into a valid polynomial.
 */
private class EquationAnalyzer(
    private val active: AtomicBoolean,
    private val onDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var lastRun = 0L
    @Volatile private var busy = false

    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        if (!active.get() || busy || now - lastRun < 700) {
            imageProxy.close()
            return
        }
        lastRun = now
        busy = true
        try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                busy = false
                imageProxy.close()
                return
            }
            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(input)
                .addOnSuccessListener { visionText ->
                    if (active.get() && PhotoEquationParser.parseEquation(visionText.text) != null) {
                        onDetected(visionText.text)
                    }
                }
                .addOnCompleteListener {
                    busy = false
                    imageProxy.close()
                }
        } catch (e: Exception) {
            busy = false
            imageProxy.close()
        }
    }
}

// =====================================================================
// TAB 2: Input — free-text field with smart auto-detection
// =====================================================================

@Composable
private fun InputTab() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<EquationResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun solveInput() {
        error = null
        try {
            result = SmartSolver.detectAndSolve(input)
        } catch (e: Exception) {
            result = null
            error = Loc["solveFailed"].let { it.format(e.message ?: Loc["unknownError"]) }
        }
    }

    val detectedType = SmartSolver.detectType(input)

    TextField(
        value = input,
        onValueChange = { input = it },
        label = { Text(text = Loc["inputHint"], color = MaterialTheme.colorScheme.primary) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { solveInput() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Indigo,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Teal
        ),
        modifier = Modifier.fillMaxWidth()
    )

    // Live detection hint
    Text(
        text = if (detectedType == null) Loc["detectionNone"]
        else Loc["detectionHint"].format(badgeLabel(detectedType)),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (detectedType == null) MaterialTheme.colorScheme.onSurfaceVariant else Teal
    )

    // Quick example chips
    Text(
        text = Loc["quickExamples"],
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("x^2-5x+6=0", "1/2+1/3", "15% от 200", "НОД 48 36", "sqrt(16)+2^3").forEach { chip ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { input = chip }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = chip,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    // Solve button
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                brush = Brush.verticalGradient(listOf(Indigo, Violet)),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { solveInput() },
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
        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
    }

    ResultSection(result)
}

// =====================================================================
// Shared result rendering
// =====================================================================

private fun badgeLabel(type: String): String = Loc[
    when (type) {
        "fraction" -> "badgeFractions"
        "percent" -> "badgePercent"
        "divisibility" -> "badgeDivisibility"
        "expression" -> "badgeExpression"
        "power" -> "badgePower"
        else -> "badgeEquation"
    }
]

@Composable
private fun ResultSection(result: EquationResult?) {
    result ?: return

    // Summary card with detected-type badge
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeLabel(result.type),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = result.summary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    // Roots / values card
    if (result.roots.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.secondaryContainer, NavySurfaceHigh)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = if (result.type == "expression") Loc["values"] else Loc["roots"],
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Teal
                )
                result.roots.forEachIndexed { i, r ->
                    Text(
                        text = if (result.type == "expression") "= ${formatRoot(r)}"
                        else "x${i + 1} = ${formatRoot(r)}",
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

    // Steps timeline
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
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

@Composable
internal fun StepTimelineItem(
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
