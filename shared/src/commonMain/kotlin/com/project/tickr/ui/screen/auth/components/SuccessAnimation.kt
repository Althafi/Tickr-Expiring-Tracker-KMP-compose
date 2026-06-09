package com.project.tickr.ui.screen.auth.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.project.tickr.ui.theme.TickrTheme

@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
) {
    var started by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.5f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "success_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "success_alpha",
    )

    LaunchedEffect(Unit) { started = true }

    val safeColor = TickrTheme.colors.safe
    val safeContainerColor = TickrTheme.colors.safeContainer

    Canvas(
        modifier = modifier
            .size(120.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
    ) {
        // Background circle
        drawCircle(color = safeContainerColor)

        // Checkmark path
        val strokeWidth = 6.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.22f

        val path = Path().apply {
            moveTo(cx - r * 0.65f, cy)
            lineTo(cx - r * 0.05f, cy + r * 0.6f)
            lineTo(cx + r * 0.8f, cy - r * 0.55f)
        }
        drawPath(
            path = path,
            color = safeColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
