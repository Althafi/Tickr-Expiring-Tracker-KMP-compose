package com.project.tickr.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.tickr.ui.theme.TickrTheme

private val VeinColor = Color(0xFFF2A22E)

/** Tulang daun dalam ruang 108x108 (sama dengan ikon launcher): batang tengah lalu 4 pasang cabang. */
private val veins: List<FloatArray> = listOf(
    floatArrayOf(54f, 72f, 54f, 32f), // dari bawah ke atas
    floatArrayOf(54f, 71f, 47f, 64f), floatArrayOf(54f, 71f, 61f, 64f),
    floatArrayOf(54f, 63f, 44f, 55f), floatArrayOf(54f, 63f, 64f, 55f),
    floatArrayOf(54f, 54f, 44f, 46f), floatArrayOf(54f, 54f, 64f, 46f),
    floatArrayOf(54f, 45f, 47f, 38f), floatArrayOf(54f, 45f, 61f, 38f),
)

/**
 * Splash dalam app yang menyambung splash sistem: daun muncul membal, tulang daunnya tergambar
 * dari bawah ke atas, lalu tulisan "Tickr" naik perlahan.
 *
 * @param onIntroFinished dipanggil sekali animasi masuk selesai.
 * @param canExit true bila isi di baliknya siap; splash baru memudar setelah intro selesai DAN [canExit].
 * @param onExited dipanggil setelah splash benar-benar hilang (pemanggil boleh melepasnya dari komposisi).
 */
@Composable
fun TickrSplash(
    canExit: Boolean,
    onIntroFinished: () -> Unit,
    onExited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TickrTheme.colors
    val pop = remember { Animatable(0f) }
    val draw = remember { Animatable(0f) }
    val text = remember { Animatable(0f) }
    val fade = remember { Animatable(1f) }
    val introDone = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Berurutan agar tiap tahap terbaca: daun → tulang daun → teks → jeda singkat.
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        draw.animateTo(1f, tween(durationMillis = 650, easing = FastOutSlowInEasing))
        text.animateTo(1f, tween(durationMillis = 450, easing = FastOutSlowInEasing))
        introDone.animateTo(1f, tween(durationMillis = 250, easing = LinearEasing))
        onIntroFinished()
    }
    // Memudar hanya bila intro sudah selesai DAN isi di baliknya siap (urutan keduanya bebas).
    val introFinished = introDone.value >= 1f
    var exiting by remember { mutableStateOf(false) }
    LaunchedEffect(canExit, introFinished) {
        if (canExit && introFinished && !exiting) {
            exiting = true
            fade.animateTo(0f, tween(durationMillis = 320, easing = FastOutSlowInEasing))
            onExited()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .alpha(fade.value)
            .background(colors.background)
            .clearAndSetSemantics { },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(modifier = Modifier.size(150.dp)) {
            drawLeaf(
                body = colors.primaryBrand,
                pop = pop.value,
                draw = draw.value,
            )
        }
        Text(
            text = "Tickr",
            color = colors.primaryBrand,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(y = (1f - text.value).times(14f).dp)
                .alpha(text.value),
        )
    }
}

private fun DrawScope.drawLeaf(body: Color, pop: Float, draw: Float) {
    val unit = size.minDimension / 108f
    val s = (0.55f + 0.45f * pop).coerceAtLeast(0f)
    scale(scale = s, pivot = center) {
        scale(scale = unit, pivot = Offset.Zero) {
            val alpha = pop.coerceIn(0f, 1f)
            val leaf = Path().apply {
                moveTo(54f, 22f)
                cubicTo(54f, 22f, 35f, 40f, 35f, 58f)
                cubicTo(35f, 68.5f, 43.5f, 77f, 54f, 77f)
                cubicTo(64.5f, 77f, 73f, 68.5f, 73f, 58f)
                cubicTo(73f, 40f, 54f, 22f, 54f, 22f)
                close()
            }
            drawPath(leaf, body.copy(alpha = alpha))
            // Tangkai + titik
            drawLine(body.copy(alpha = alpha), Offset(54f, 77f), Offset(54f, 83f), strokeWidth = 2.4f, cap = StrokeCap.Round)
            drawCircle(body.copy(alpha = alpha), radius = 3f, center = Offset(54f, 82f))

            // Tulang daun: tiap garis mendapat jatah waktu bergeser agar tergambar berurutan.
            veins.forEachIndexed { i, v ->
                val start = i * 0.085f
                val f = ((draw - start) / 0.35f).coerceIn(0f, 1f)
                if (f > 0f) {
                    val end = Offset(v[0] + (v[2] - v[0]) * f, v[1] + (v[3] - v[1]) * f)
                    drawLine(VeinColor, Offset(v[0], v[1]), end, strokeWidth = 2f, cap = StrokeCap.Round)
                }
            }
        }
    }
}
