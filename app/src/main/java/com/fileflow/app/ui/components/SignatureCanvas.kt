package com.fileflow.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fileflow.app.ui.theme.ToolCardShape

data class SignaturePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun SignatureCanvas(
    onSignatureChanged: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberAppHaptics()
    val paths = remember { mutableStateListOf<SignaturePath>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(Color(0xFF0F172A)) }

    val colorOptions = listOf(
        Color(0xFF0F172A), // Black
        Color(0xFF1E3A8A), // Navy Blue
        Color(0xFF881337)  // Deep Wine
    )

    fun renderToBitmap(): Bitmap? {
        if (paths.isEmpty()) return null
        val width = 600
        val height = 300
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val androidCanvas = AndroidCanvas(bitmap)

        paths.forEach { sigPath ->
            if (sigPath.points.size > 1) {
                val paint = AndroidPaint().apply {
                    color = sigPath.color.toArgb()
                    style = AndroidPaint.Style.STROKE
                    strokeWidth = sigPath.strokeWidth * 1.5f
                    strokeCap = AndroidPaint.Cap.ROUND
                    strokeJoin = AndroidPaint.Join.ROUND
                    isAntiAlias = true
                }
                val path = android.graphics.Path()
                path.moveTo(sigPath.points.first().x, sigPath.points.first().y)
                for (i in 1 until sigPath.points.size) {
                    path.lineTo(sigPath.points[i].x, sigPath.points[i].y)
                }
                androidCanvas.drawPath(path, paint)
            }
        }
        return bitmap
    }

    Surface(
        shape = ToolCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Draw Signature",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Ink color picker
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    2.dp,
                                    if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    haptics.tap()
                                    selectedColor = color
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Drawing Pad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                    .pointerInput(selectedColor) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    paths.add(SignaturePath(currentPoints, selectedColor, 6f))
                                    currentPoints = emptyList()
                                    onSignatureChanged(renderToBitmap())
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw existing paths
                    paths.forEach { sigPath ->
                        if (sigPath.points.size > 1) {
                            val path = Path()
                            path.moveTo(sigPath.points.first().x, sigPath.points.first().y)
                            for (i in 1 until sigPath.points.size) {
                                path.lineTo(sigPath.points[i].x, sigPath.points[i].y)
                            }
                            drawPath(
                                path = path,
                                color = sigPath.color,
                                style = Stroke(
                                    width = sigPath.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    // Draw active path
                    if (currentPoints.size > 1) {
                        val activePath = Path()
                        activePath.moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            activePath.lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                        drawPath(
                            path = activePath,
                            color = selectedColor,
                            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }

                if (paths.isEmpty() && currentPoints.isEmpty()) {
                    Text(
                        text = "Sign here with your finger",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.tap()
                        if (paths.isNotEmpty()) {
                            paths.removeAt(paths.size - 1)
                            onSignatureChanged(renderToBitmap())
                        }
                    },
                    shape = CircleShape,
                    enabled = paths.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = {
                        haptics.heavyTap()
                        paths.clear()
                        currentPoints = emptyList()
                        onSignatureChanged(null)
                    },
                    shape = CircleShape,
                    enabled = paths.isNotEmpty()
                ) {
                    Icon(Icons.Rounded.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
    }
}
