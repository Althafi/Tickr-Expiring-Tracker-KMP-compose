package com.project.tickr.ui.screen.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.ob1_arm
import tickrapp.shared.generated.resources.ob1_base
import tickrapp.shared.generated.resources.ob1_eyes
import tickrapp.shared.generated.resources.ob1_plant
import tickrapp.shared.generated.resources.ob2_arm
import tickrapp.shared.generated.resources.ob2_base
import tickrapp.shared.generated.resources.ob2_emblem
import tickrapp.shared.generated.resources.ob2_eyes
import tickrapp.shared.generated.resources.ob2_plant
import tickrapp.shared.generated.resources.ob3_badge
import tickrapp.shared.generated.resources.ob3_base
import tickrapp.shared.generated.resources.ob3_bell
import tickrapp.shared.generated.resources.ob3_bubble
import tickrapp.shared.generated.resources.ob3_eyes
import tickrapp.shared.generated.resources.ob3_plant
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

/*
 * Ilustrasi onboarding: satu gambar utama (ob{n}_base.png, 500x500) + beberapa layer PNG hasil potongan
 * dari gambar yang sama (ob{n}_arm/eyes/plant/...) dengan ukuran kanvas identik, ditumpuk persis di atas
 * gambar utama. Bagian yang dipotong sudah dihapus dari gambar utama, jadi saat layer bergerak tidak ada
 * bayangan ganda. Semua pivot memakai ruang 500x500 yang sama dengan file gambarnya.
 *
 *  - `time`  : detik berulang 0..12 (periode gerak dipilih pembagi 12 supaya mulus saat mengulang).
 *  - `intro` : 0..1, diputar ulang setiap halaman menjadi aktif (animasi masuk bertahap).
 * Keduanya dibaca di dalam lambda graphicsLayer sehingga hanya memicu fase gambar, bukan recomposition.
 */

private const val SPACE = 500f
private val RippleColor = Color(0xFFB7DFAA)

@Composable
fun OnboardingIllustration(page: Int, active: Boolean, modifier: Modifier = Modifier) {
    when (page) {
        0 -> WelcomeIllustration(active, modifier)
        1 -> TrackIllustration(active, modifier)
        else -> NotifyIllustration(active, modifier)
    }
}

// ---------------------------------------------------------------- helpers

@Composable
private fun rememberSceneTime(): State<Float> =
    rememberInfiniteTransition(label = "scene-time").animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 12_000, easing = LinearEasing)),
        label = "t",
    )

@Composable
private fun rememberIntro(active: Boolean): State<Float> {
    val intro = remember { Animatable(0f) }
    LaunchedEffect(active) {
        if (active) {
            intro.snapTo(0f)
            intro.animateTo(1f, tween(durationMillis = 1600, easing = LinearEasing))
        }
    }
    return intro.asState()
}

private fun seg(t: Float, a: Float, b: Float): Float = ((t - a) / (b - a)).coerceIn(0f, 1f)
private fun easeOut(x: Float): Float = 1f - (1f - x) * (1f - x) * (1f - x)
private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val p = x - 1f
    return 1f + c3 * p * p * p + c1 * p * p
}

private fun wave(t: Float, period: Float, phase: Float = 0f): Float =
    sin(2f * PI.toFloat() * t / period + phase)

private fun GraphicsLayerScope.pivot(x: Float, y: Float) {
    transformOrigin = TransformOrigin(x / SPACE, y / SPACE)
}

/** Satuan piksel untuk 1 unit ruang 500x500 pada ukuran gambar saat ini. */
private val GraphicsLayerScope.unit: Float get() = size.width / SPACE

@Composable
private fun Part(res: DrawableResource, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun Scene(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.aspectRatio(1f), content = content)
}

/** Seluruh gambar naik dari bawah sambil membesar dan memudar masuk, lalu "bernapas" pelan. */
private fun Modifier.figure(time: State<Float>, intro: State<Float>) = graphicsLayer {
    val p = easeOut(seg(intro.value, 0f, 0.5f))
    val s = 0.9f + 0.1f * p
    alpha = p
    scaleX = s
    scaleY = s
    translationY = (1f - p) * 50f * unit + 1.8f * unit * wave(time.value, 4f)
    rotationZ = 0.5f * wave(time.value, 6f)
    pivot(250f, 440f)
}

/** Tumbuh dari titik ([px],[py]) dengan sedikit membal antara intro [a]..[b]. */
private fun Modifier.popIn(intro: State<Float>, a: Float, b: Float, px: Float, py: Float) = graphicsLayer {
    val p = seg(intro.value, a, b)
    val s = easeOutBack(p).coerceAtLeast(0f)
    scaleX = s
    scaleY = s
    alpha = (p * 4f).coerceIn(0f, 1f)
    pivot(px, py)
}

/** Kedipan tiap 3 detik: kelopak menutup cepat lalu membuka. */
private fun Modifier.blink(time: State<Float>, cx: Float, cy: Float) = graphicsLayer {
    val m = time.value % 3f
    val closed = if (m > 2.75f) sin(PI.toFloat() * seg(m, 2.75f, 2.95f)) else 0f
    scaleY = max(0.08f, 1f - closed)
    pivot(cx, cy)
}

// ---------------------------------------------------------------- bagian bersama

/** Daun tumbuh dari pangkal lalu bergoyang tertiup angin lembut. */
@Composable
private fun Plant(res: DrawableResource, time: State<Float>, intro: State<Float>) {
    Part(
        res,
        Modifier.graphicsLayer {
            val s = easeOutBack(seg(intro.value, 0.25f, 0.6f)).coerceAtLeast(0f)
            scaleX = s
            scaleY = s
            rotationZ = 2.5f * wave(time.value, 4f) * seg(intro.value, 0.6f, 1f)
            pivot(68f, 388f)
        },
    )
}

/**
 * Lambaian: tangan terangkat saat intro, lalu 3 detik melambai bolak-balik dengan amplitudo
 * naik-turun (berporos di pergelangan), jeda 3 detik, ulangi.
 */
@Composable
private fun WaveArm(res: DrawableResource, time: State<Float>, intro: State<Float>) {
    Part(
        res,
        Modifier.graphicsLayer {
            val raise = easeOutBack(seg(intro.value, 0.45f, 0.8f))
            val m = time.value % 6f
            val swing = if (m < 3f) 13f * wave(m, 0.75f) * sin(PI.toFloat() * m / 3f) else 0f
            rotationZ = (1f - raise) * 30f + swing * seg(intro.value, 0.8f, 1f)
            alpha = seg(intro.value, 0.4f, 0.55f)
            pivot(435f, 272f)
        },
    )
}

// ---------------------------------------------------------------- adegan 1

@Composable
private fun WelcomeIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    Scene(modifier) {
        Box(Modifier.fillMaxSize().figure(time, intro)) {
            Part(Res.drawable.ob1_base)
            Plant(Res.drawable.ob1_plant, time, intro)
            WaveArm(Res.drawable.ob1_arm, time, intro)
            Part(Res.drawable.ob1_eyes, Modifier.blink(time, 276f, 149f))
        }
    }
}

// ---------------------------------------------------------------- adegan 2

@Composable
private fun TrackIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    Scene(modifier) {
        Box(Modifier.fillMaxSize().figure(time, intro)) {
            Part(Res.drawable.ob2_base)
            Plant(Res.drawable.ob2_plant, time, intro)
            Part(
                Res.drawable.ob2_emblem,
                Modifier.graphicsLayer {
                    // Emblem daur ulang berputar terus (12 dtk = 1 putaran) sambil berdenyut.
                    val s = easeOutBack(seg(intro.value, 0.5f, 0.8f)).coerceAtLeast(0f)
                    val pulse = 1f + 0.05f * wave(time.value, 3f)
                    scaleX = s * pulse
                    scaleY = s * pulse
                    rotationZ = 360f * time.value / 12f
                    pivot(263f, 333f)
                },
            )
            WaveArm(Res.drawable.ob2_arm, time, intro)
            Part(Res.drawable.ob2_eyes, Modifier.blink(time, 283f, 151f))
        }
    }
}

// ---------------------------------------------------------------- adegan 3

@Composable
private fun NotifyIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    // Satu siklus 3 detik: notifikasi masuk (1.2) → lonceng berdering → lencana berdenyut → riak memuai.
    Scene(modifier) {
        Box(Modifier.fillMaxSize().figure(time, intro)) {
            Part(Res.drawable.ob3_base)
            Plant(Res.drawable.ob3_plant, time, intro)

            // Riak notifikasi: tiga cincin memuai dan memudar bergantian dari gelembung.
            Canvas(Modifier.fillMaxSize()) {
                val u = size.width / SPACE
                val gate = seg(intro.value, 0.75f, 0.95f)
                repeat(3) { k ->
                    val p = ((time.value / 2f + k / 3f) % 1f)
                    drawCircle(
                        color = RippleColor.copy(alpha = ((1f - p) * 0.7f * gate).coerceIn(0f, 1f)),
                        radius = (48f + 42f * p) * u,
                        center = Offset(418f * u, 200f * u),
                        style = Stroke(width = 2.6f * u),
                    )
                }
            }

            Part(Res.drawable.ob3_bubble, Modifier.popIn(intro, 0.45f, 0.8f, 395f, 255f))
            Part(
                Res.drawable.ob3_bell,
                Modifier.graphicsLayer {
                    val pop = easeOutBack(seg(intro.value, 0.55f, 0.88f)).coerceAtLeast(0f)
                    scaleX = pop
                    scaleY = pop
                    // Lonceng berdering: ayunan teredam setelah notifikasi masuk, lalu diam.
                    val m = (time.value % 3f) - 1.2f
                    rotationZ = if (m in 0f..1.7f) 22f * exp(-2.4f * m) * sin(2f * PI.toFloat() * 2.8f * m) else 0f
                    pivot(423f, 172f)
                },
            )
            Part(
                Res.drawable.ob3_badge,
                Modifier.graphicsLayer {
                    val pop = easeOutBack(seg(intro.value, 0.7f, 0.95f)).coerceAtLeast(0f)
                    val m = (time.value % 3f) - 1.2f
                    val beat = if (m in 0f..0.5f) 0.3f * sin(PI.toFloat() * m / 0.5f) else 0f
                    scaleX = pop * (1f + beat)
                    scaleY = pop * (1f + beat)
                    pivot(441f, 186f)
                },
            )
            Part(Res.drawable.ob3_eyes, Modifier.blink(time, 282f, 152f))
        }
    }
}
