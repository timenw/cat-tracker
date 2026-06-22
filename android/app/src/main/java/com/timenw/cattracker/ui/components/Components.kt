package com.timenw.cattracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timenw.cattracker.ui.theme.*

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, emoji: String = "🐱") {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyStateView(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StatBar(
    label: String,
    value: Int,
    maxValue: Int = 100,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        val progress = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            drawRect(
                color = Color.Gray.copy(alpha = 0.2f),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
            drawRect(
                color = color,
                topLeft = Offset.Zero,
                size = Size(size.width * progress, size.height)
            )
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    value: Int,
    maxValue: Int,
    size: Int = 140,
    strokeWidth: Int = 10,
    color: Color = CatSafe,
    label: String = ""
) {
    val clampedProgress = progress.coerceIn(0f, 1.5f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp)) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val stroke = Stroke(width = strokeWidth.dp.toPx(), cap = StrokeCap.Round)
            val radius = (size.dp.toPx() - stroke.width) / 2
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = Offset(size.dp.toPx() / 2, size.dp.toPx() / 2),
                style = stroke
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (clampedProgress * 360f).coerceAtMost(360f),
                useCenter = false,
                topLeft = Offset(size.dp.toPx() / 2 - radius, size.dp.toPx() / 2 - radius),
                size = Size(radius * 2, radius * 2),
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$value",
                fontSize = (size / 5).sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    fontSize = (size / 12).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CooldownTimer(remainingMs: Long) {
    val seconds = remainingMs / 1000
    val minutes = seconds / 60
    val secs = seconds % 60
    Text(
        text = String.format("%02d:%02d", minutes, secs),
        style = MaterialTheme.typography.labelSmall,
        color = CatWarning
    )
}
