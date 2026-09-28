package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

/**
 * Animated and Interactive Jetpack Compose Charts
 */

@Composable
fun BarChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    onValueSelected: (Int, Double) -> Unit = { _, _ -> }
) {
    var selectedIndex by remember { mutableStateOf(-1) }
    val maxVal = max(1.0, data.maxOrNull() ?: 1.0)
    
    var animationTriggered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val numBars = data.size
                                val barWidth = width / (numBars * 1.5f)
                                val spacing = barWidth * 0.5f

                                for (i in 0 until numBars) {
                                    val left = spacing + i * (barWidth + spacing)
                                    val right = left + barWidth
                                    if (offset.x in left..right) {
                                        selectedIndex = i
                                        onValueSelected(i, data[i])
                                        break
                                    }
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val numBars = data.size
                    
                    val barWidth = canvasWidth / (numBars * 1.5f)
                    val spacing = barWidth * 0.5f

                    // Draw vertical and horizontal axis
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 2f
                    )

                    // Draw grid lines (3 levels)
                    val gridLevels = 3
                    for (g in 1..gridLevels) {
                        val y = canvasHeight - (canvasHeight * (g.toFloat() / gridLevels))
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    // Draw Bars
                    for (i in 0 until numBars) {
                        val barHeight = ((data[i] / maxVal) * canvasHeight * progress).toFloat()
                        val left = (spacing + i * (barWidth + spacing)).toFloat()
                        val top = (canvasHeight - barHeight).toFloat()

                        val isSelected = selectedIndex == i
                        val color = if (isSelected) barColor.copy(alpha = 0.8f) else barColor

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Highlight Border if selected
                        if (isSelected) {
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8f, 8f),
                                style = Stroke(width = 3f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEachIndexed { index, label ->
                    val isSelected = selectedIndex == index
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                selectedIndex = index
                                onValueSelected(index, data[index])
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<Double>,
    labels: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val total = max(1.0, data.sum())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    data.forEachIndexed { index, value ->
                        val sweepAngle = ((value / total) * 360f).toFloat() * progress
                        drawArc(
                            color = colors[index % colors.size],
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 36f, cap = StrokeCap.Round),
                            size = Size(size.width - 36f, size.height - 36f),
                            topLeft = Offset(18f, 18f)
                        )
                        startAngle += sweepAngle
                    }
                }
                
                // Inner label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Demand",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.0f m³", total),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Legends list
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                data.forEachIndexed { index, valItem ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(colors[index % colors.size], RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = labels[index],
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format("%.1f m³/day (%.0f%%)", valItem, (valItem / total) * 100),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Double>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val maxVal = max(1.0, data.maxOrNull() ?: 1.0)
    val minVal = data.minOrNull() ?: 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw axis grid lines
                    val gridLevels = 3
                    for (g in 0..gridLevels) {
                        val y = canvasHeight * (g.toFloat() / gridLevels)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    if (data.size > 1) {
                        val stepX = canvasWidth / (data.size - 1)
                        val points = data.mapIndexed { idx, value ->
                            val ratio = if (maxVal - minVal > 0) (value - minVal) / (maxVal - minVal) else 0.5
                            val y = canvasHeight - (ratio * canvasHeight * progress).toFloat()
                            Offset(idx * stepX, y)
                        }

                        val path = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                // Simple Bezier Curve
                                val p0 = points[i - 1]
                                val p1 = points[i]
                                val controlX = (p0.x + p1.x) / 2
                                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        }

                        // Gradient Area fill below line
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, canvasHeight)
                            lineTo(points.first().x, canvasHeight)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = canvasHeight
                            )
                        )

                        // Draw main line path
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw point indicator dots
                        points.forEach { point ->
                            drawCircle(
                                color = lineColor,
                                radius = 6f,
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = point
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { label ->
                    Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun RadarChart(
    values: List<Double>, // 5 values normalized 0.0 to 1.0 (recharge, storage, pumping, risk, factor)
    labels: List<String>,
    modifier: Modifier = Modifier,
    webColor: Color = MaterialTheme.colorScheme.secondary,
    accentColor: Color = MaterialTheme.colorScheme.tertiary
) {
    var animationTriggered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val r = size.width / 2.3f
                    val numPoints = values.size

                    // Draw spiderweb grid (3 circles of different radii)
                    val webLevels = 3
                    for (level in 1..webLevels) {
                        val radius = r * (level.toFloat() / webLevels)
                        val path = Path()
                        for (i in 0 until numPoints) {
                            val angle = (2 * PI * i / numPoints) - (PI / 2)
                            val x = (center.x + radius * cos(angle)).toFloat()
                            val y = (center.y + radius * sin(angle)).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = webColor.copy(alpha = 0.3f),
                            style = Stroke(width = 1.5f)
                        )
                    }

                    // Draw axis rays
                    for (i in 0 until numPoints) {
                        val angle = (2 * PI * i / numPoints) - (PI / 2)
                        val end = Offset(
                            (center.x + r * cos(angle)).toFloat(),
                            (center.y + r * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = webColor.copy(alpha = 0.2f),
                            start = center,
                            end = end,
                            strokeWidth = 2f
                        )
                    }

                    // Draw actual data polygon
                    if (numPoints > 1) {
                        val valuePath = Path()
                        val valuePoints = mutableListOf<Offset>()

                        for (i in 0 until numPoints) {
                            val angle = (2 * PI * i / numPoints) - (PI / 2)
                            val distance = values[i] * r * progress
                            val x = (center.x + distance * cos(angle)).toFloat()
                            val y = (center.y + distance * sin(angle)).toFloat()
                            
                            val offset = Offset(x, y)
                            valuePoints.add(offset)
                            
                            if (i == 0) valuePath.moveTo(x, y) else valuePath.lineTo(x, y)
                        }
                        valuePath.close()

                        // Fill translucent
                        drawPath(
                            path = valuePath,
                            color = accentColor.copy(alpha = 0.3f)
                        )

                        // Outline path
                        drawPath(
                            path = valuePath,
                            color = accentColor,
                            style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Data nodes
                        valuePoints.forEach { pt ->
                            drawCircle(
                                color = accentColor,
                                radius = 4f,
                                center = pt
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-row legends for radar attributes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                labels.forEachIndexed { idx, label ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accentColor.copy(alpha = (0.2f + 0.16f * idx).coerceAtMost(1f)), RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format("%.0f%%", values[idx] * 100), fontSize = 10.sp, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
