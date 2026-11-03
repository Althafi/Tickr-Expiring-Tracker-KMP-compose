package com.project.tickr.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.project.tickr.presentation.common.MessageKind

/**
 * Ikon status beranimasi: lingkaran muncul membal, lalu tanda centang (SUCCESS) atau silang (ERROR)
 * tergambar bertahap. Silang ditutup dengan getaran kecil agar terasa sebagai peringatan.
 *
 * Gambar vektornya sama dengan `ic_register_success` / `ic_register_failed` (cincin + lingkaran putih
 * + tanda), hanya digambar lewat Canvas supaya tiap bagiannya bisa dianimasikan.
 *
 * @param playKey animasi diputar ulang setiap nilainya berubah.
 */
@Composable
fun AnimatedStatusIcon(
    kind: MessageKind,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    playKey: Any = kind,
) {
    val isError = kind == MessageKind.ERROR
    val ringColor = if (isError) Color(0xFFEF5350) else Color(0xFF66BB6A)
    val shadowColor = if (isError) Color(0xFFC62828) else Color(0xFF388E3C)

    val pop = remember(playKey) { Animatable(0f) }      // cincin membal masuk
    val draw = remember(playKey) { Animatable(0f) }     // tanda tergambar 0..1
    val shake = remember(playKey) { Animatable(0f) }    // getaran silang

    LaunchedEffect(playKey) {
        pop.snapTo(0f); draw.snapTo(0f); shake.snapTo(0f)
        pop.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        )
        draw.animateTo(1f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
        if (isError) {
            // ±6° lalu tenang
            for (target in floatArrayOf(6f, -6f, 4f, -3f, 0f)) {
                shake.animateTo(target, tween(durationMillis = 55))
            }
        }
    }

    // Path tanda dibuat dalam ruang 100x100 sesuai vektor aslinya, lalu diskalakan di Canvas.
    val checkPath = remember {
        Path().apply {
            moveTo(32f, 51f); lineTo(43f, 64f); lineTo(70f, 36f)
        }
    }
    val crossA = remember { Path().apply { moveTo(37f, 37f); lineTo(63f, 63f) } }
    val crossB = remember { Path().apply { moveTo(63f, 37f); lineTo(37f, 63f) } }
    val measureCheck = remember { PathMeasure().apply { setPath(checkPath, false) } }
    val measureA = remember { PathMeasure().apply { setPath(crossA, false) } }
    val measureB = remember { PathMeasure().apply { setPath(crossB, false) } }

    Canvas(modifier = modifier.size(size).clearAndSetSemantics { }) {
        val unit = this.size.minDimension / 100f
        val popScale = pop.value.coerceAtLeast(0f)

        scale(scale = popScale, pivot = center) {
            drawCircle(shadowColor, radius = 47f * unit, center = center)
            drawCircle(ringColor, radius = 43f * unit, center = center)
            drawCircle(Color.White, radius = 23f * unit, center = center)

            rotate(degrees = shake.value, pivot = center) {
                scale(scale = unit, pivot = Offset.Zero) {
                    // Lebar 7 di ruang 100x100; ikut membesar bersama skala.
                    val strokeUnit = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    if (!isError) {
                        drawPartial(measureCheck, draw.value, ringColor, strokeUnit)
                    } else {
                        // Dua garis digambar berurutan: separuh pertama garis A, separuh kedua garis B.
                        drawPartial(measureA, (draw.value * 2f).coerceIn(0f, 1f), ringColor, strokeUnit)
                        drawPartial(measureB, ((draw.value - 0.5f) * 2f).coerceIn(0f, 1f), ringColor, strokeUnit)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawPartial(
    measure: PathMeasure,
    fraction: Float,
    color: Color,
    stroke: Stroke,
) {
    if (fraction <= 0f) return
    val segment = Path()
    measure.getSegment(0f, measure.length * fraction, segment, startWithMoveTo = true)
    drawPath(segment, color, style = stroke)
}
