package com.project.tickr.ui.screen.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import tickrapp.shared.generated.resources.Res
import tickrapp.shared.generated.resources.ill1_arm_upper
import tickrapp.shared.generated.resources.ill1_arm_wave
import tickrapp.shared.generated.resources.ill1_blob
import tickrapp.shared.generated.resources.ill1_body
import tickrapp.shared.generated.resources.ill1_eyes
import tickrapp.shared.generated.resources.ill1_face
import tickrapp.shared.generated.resources.ill1_ground
import tickrapp.shared.generated.resources.ill1_hair_back
import tickrapp.shared.generated.resources.ill1_hair_front
import tickrapp.shared.generated.resources.ill1_leaf_big
import tickrapp.shared.generated.resources.ill1_leaf_left
import tickrapp.shared.generated.resources.ill1_leaf_right
import tickrapp.shared.generated.resources.ill1_mouth
import tickrapp.shared.generated.resources.ill1_neck
import tickrapp.shared.generated.resources.ill1_pot
import tickrapp.shared.generated.resources.ill1_rest_hand
import tickrapp.shared.generated.resources.ill2_arm_upper
import tickrapp.shared.generated.resources.ill2_arm_wave
import tickrapp.shared.generated.resources.ill2_blob
import tickrapp.shared.generated.resources.ill2_body
import tickrapp.shared.generated.resources.ill2_chair
import tickrapp.shared.generated.resources.ill2_emblem
import tickrapp.shared.generated.resources.ill2_eyes
import tickrapp.shared.generated.resources.ill2_face
import tickrapp.shared.generated.resources.ill2_hair
import tickrapp.shared.generated.resources.ill2_laptop_base
import tickrapp.shared.generated.resources.ill2_laptop_screen
import tickrapp.shared.generated.resources.ill2_mouth
import tickrapp.shared.generated.resources.ill2_neck
import tickrapp.shared.generated.resources.ill2_notebook
import tickrapp.shared.generated.resources.ill2_screen_lines
import tickrapp.shared.generated.resources.ill2_write_arm
import tickrapp.shared.generated.resources.ill2_writer
import tickrapp.shared.generated.resources.ill3_arm
import tickrapp.shared.generated.resources.ill3_badge
import tickrapp.shared.generated.resources.ill3_bell
import tickrapp.shared.generated.resources.ill3_bubble
import tickrapp.shared.generated.resources.ill3_mouth
import tickrapp.shared.generated.resources.ill3_phone
import tickrapp.shared.generated.resources.ill3_phone_glow
import tickrapp.shared.generated.resources.ill3_ripple
import tickrapp.shared.generated.resources.ill3_tap_hand
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

/*
 * Ilustrasi onboarding berupa vector drawable per bagian (500x500, ditumpuk persis) yang masing-masing
 * dianimasikan sendiri. Semua koordinat pivot memakai ruang 500x500 yang sama dengan file vektornya.
 *
 *  - `time`  : detik berulang 0..12 (periode gerak dipilih pembagi 12 supaya mulus saat mengulang).
 *  - `intro` : 0..1, diputar ulang setiap halaman menjadi aktif (animasi masuk bertahap).
 * Keduanya dibaca di dalam lambda graphicsLayer sehingga hanya memicu fase gambar, bukan recomposition.
 */

private const val SPACE = 500f

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

/** Muncul dari bawah sambil memudar masuk antara intro [a]..[b]. */
private fun Modifier.riseIn(intro: State<Float>, a: Float, b: Float, dy: Float = 48f) = graphicsLayer {
    val p = easeOut(seg(intro.value, a, b))
    alpha = p
    translationY = (1f - p) * dy * unit
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

// ---------------------------------------------------------------- bagian bersama

@Composable
private fun BoxScope.BlobBack(res: DrawableResource, time: State<Float>, intro: State<Float>) {
    Part(
        res,
        Modifier.graphicsLayer {
            val s = easeOutBack(seg(intro.value, 0f, 0.38f)).coerceAtLeast(0f)
            val breathe = 1f + 0.03f * wave(time.value, 6f)
            scaleX = s * breathe
            scaleY = s * breathe
            rotationZ = 2.2f * wave(time.value, 12f)
            pivot(270f, 260f)
        },
    )
}

@Composable
private fun BoxScope.PlantAndGround(time: State<Float>, intro: State<Float>) {
    Part(
        Res.drawable.ill1_ground,
        Modifier.graphicsLayer {
            scaleX = easeOut(seg(intro.value, 0.02f, 0.3f))
            pivot(250f, 440f)
        },
    )
    Part(Res.drawable.ill1_pot, Modifier.riseIn(intro, 0.12f, 0.38f, dy = 70f))
    PlantLeaf(Res.drawable.ill1_leaf_big, time, intro, 0.28f, 0.58f, amp = 3f, phase = 0f)
    PlantLeaf(Res.drawable.ill1_leaf_right, time, intro, 0.38f, 0.68f, amp = 5f, phase = 1.3f)
    PlantLeaf(Res.drawable.ill1_leaf_left, time, intro, 0.48f, 0.78f, amp = 5f, phase = 2.6f)
}

@Composable
private fun BoxScope.PlantLeaf(
    res: DrawableResource,
    time: State<Float>,
    intro: State<Float>,
    a: Float,
    b: Float,
    amp: Float,
    phase: Float,
) {
    Part(
        res,
        Modifier.graphicsLayer {
            val s = easeOutBack(seg(intro.value, a, b)).coerceAtLeast(0f)
            scaleX = s
            scaleY = s
            // Daun tumbuh dari pangkal lalu bergoyang tertiup angin lembut.
            rotationZ = amp * wave(time.value, 4f, phase) * seg(intro.value, b, 1f)
            pivot(68f, 390f)
        },
    )
}

/** Kepala bergoyang pelan (menoleh kecil) mengelilingi leher. */
private fun Modifier.headMotion(time: State<Float>, tiltDeg: Float = 2f) = graphicsLayer {
    rotationZ = tiltDeg * wave(time.value, 6f)
    translationX = 2f * unit * wave(time.value, 6f, 1.2f)
    pivot(262f, 280f)
}

/** Kedipan tiap 3 detik: kelopak menutup cepat lalu membuka. */
private fun Modifier.blink(time: State<Float>, cx: Float, cy: Float) = graphicsLayer {
    val m = time.value % 3f
    val closed = if (m > 2.75f) sin(PI.toFloat() * seg(m, 2.75f, 2.95f)) else 0f
    scaleY = max(0.08f, 1f - closed)
    pivot(cx, cy)
}

/** Napas: seluruh badan naik-turun sangat halus. */
private fun Modifier.breath(time: State<Float>) = graphicsLayer {
    translationY = 2.5f * unit * wave(time.value, 4f)
}

// ---------------------------------------------------------------- adegan 1

@Composable
private fun WelcomeIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    Scene(modifier) {
        BlobBack(Res.drawable.ill1_blob, time, intro)
        PlantAndGround(time, intro)

        Box(Modifier.fillMaxSize().riseIn(intro, 0.22f, 0.62f, dy = 60f).breath(time)) {
            Part(Res.drawable.ill1_hair_back, Modifier.headMotion(time))
            Part(Res.drawable.ill1_body)
            Part(Res.drawable.ill1_arm_upper)
            Part(Res.drawable.ill1_rest_hand)
            Box(Modifier.fillMaxSize().headMotion(time)) {
                Part(Res.drawable.ill1_neck)
                Part(Res.drawable.ill1_face)
                Part(Res.drawable.ill1_hair_front)
                Part(Res.drawable.ill1_eyes, Modifier.blink(time, 263f, 166f))
                Part(
                    Res.drawable.ill1_mouth,
                    Modifier.graphicsLayer {
                        // Senyum melebar sedikit saat melambai.
                        scaleX = 1f + 0.07f * max(0f, wave(time.value, 6f, -1.2f))
                        pivot(271f, 200f)
                    },
                )
            }
            WaveArm(Res.drawable.ill1_arm_wave, time, intro)
        }
    }
}

/**
 * Lambaian: lengan terangkat dari bawah saat intro, lalu 3 detik melambai bolak-balik
 * dengan amplitudo naik-turun, jeda 3 detik, ulangi.
 */
@Composable
private fun BoxScope.WaveArm(res: DrawableResource, time: State<Float>, intro: State<Float>) {
    Part(
        res,
        Modifier.graphicsLayer {
            val raise = easeOutBack(seg(intro.value, 0.5f, 0.85f))
            val m = time.value % 6f
            val swing = if (m < 3f) 15f * wave(m, 0.75f) * sin(PI.toFloat() * m / 3f) else 0f
            rotationZ = (1f - raise) * 55f + swing * seg(intro.value, 0.85f, 1f)
            alpha = seg(intro.value, 0.45f, 0.6f)
            pivot(398f, 392f)
        },
    )
}

// ---------------------------------------------------------------- adegan 2

@Composable
private fun TrackIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    Scene(modifier) {
        BlobBack(Res.drawable.ill2_blob, time, intro)
        Part(Res.drawable.ill2_chair, Modifier.riseIn(intro, 0.1f, 0.4f, dy = 30f))
        PlantAndGround(time, intro)

        Box(Modifier.fillMaxSize().riseIn(intro, 0.22f, 0.62f, dy = 60f).breath(time)) {
            Part(Res.drawable.ill2_body)
            Part(
                Res.drawable.ill2_emblem,
                Modifier.graphicsLayer {
                    // Emblem daur ulang berputar terus (12 dtk = 1 putaran) sambil berdenyut.
                    val s = easeOutBack(seg(intro.value, 0.5f, 0.8f)).coerceAtLeast(0f)
                    val pulse = 1f + 0.06f * wave(time.value, 3f)
                    scaleX = s * pulse
                    scaleY = s * pulse
                    rotationZ = 360f * time.value / 12f
                    pivot(262f, 350f)
                },
            )
            Part(Res.drawable.ill2_arm_upper)
            WaveArm(Res.drawable.ill2_arm_wave, time, intro)
            Part(Res.drawable.ill2_write_arm)

            // Laptop: layar menyala, baris teks "diketik" bertahap lalu diulang.
            Part(Res.drawable.ill2_laptop_base)
            Part(Res.drawable.ill2_laptop_screen, Modifier.riseIn(intro, 0.55f, 0.85f, dy = 24f))
            Part(
                Res.drawable.ill2_screen_lines,
                Modifier.graphicsLayer {
                    val m = time.value % 4f
                    // Tersendat per kata agar terasa seperti mengetik, bukan wipe linear.
                    val typed = (seg(m, 0.2f, 3.2f) * 14f).toInt() / 14f
                    scaleX = typed * seg(intro.value, 0.8f, 0.95f)
                    pivot(372f, 380f)
                },
            )
            Part(Res.drawable.ill2_notebook, Modifier.riseIn(intro, 0.5f, 0.8f, dy = 20f))
            Part(
                Res.drawable.ill2_writer,
                Modifier.graphicsLayer {
                    // Tangan + pena menulis: goresan kecil cepat ke kanan-kiri sambil bergeser sepanjang baris.
                    val t = time.value
                    translationX = (7f * wave(t, 0.45f) + 9f * wave(t, 4f)) * unit
                    translationY = 2f * unit * wave(t, 0.225f)
                    rotationZ = 3f * wave(t, 0.45f, 0.6f)
                    pivot(276f, 414f)
                },
            )

            Box(Modifier.fillMaxSize().headMotion(time, tiltDeg = 2.4f)) {
                Part(Res.drawable.ill2_neck)
                Part(Res.drawable.ill2_face)
                Part(Res.drawable.ill2_hair)
                Part(Res.drawable.ill2_eyes, Modifier.blink(time, 265f, 174f))
                Part(Res.drawable.ill2_mouth)
            }
        }
    }
}

// ---------------------------------------------------------------- adegan 3

@Composable
private fun NotifyIllustration(active: Boolean, modifier: Modifier) {
    val time = rememberSceneTime()
    val intro = rememberIntro(active)
    // Satu siklus 3 detik: ketuk (1.0-1.3) → layar menyala → lonceng berdering → lencana berdenyut.
    Scene(modifier) {
        BlobBack(Res.drawable.ill1_blob, time, intro)
        PlantAndGround(time, intro)

        Box(Modifier.fillMaxSize().riseIn(intro, 0.22f, 0.62f, dy = 60f).breath(time)) {
            Part(Res.drawable.ill1_hair_back, Modifier.headMotion(time))
            Part(Res.drawable.ill1_body)
            Part(Res.drawable.ill1_rest_hand)
            Part(Res.drawable.ill3_phone, Modifier.riseIn(intro, 0.45f, 0.7f, dy = 20f))
            Part(
                Res.drawable.ill3_phone_glow,
                Modifier.graphicsLayer {
                    val m = time.value % 3f
                    val p = seg(m, 1.1f, 1.9f)
                    alpha = sin(PI.toFloat() * p) * seg(intro.value, 0.8f, 1f)
                    val s = 1f + 0.55f * p
                    scaleX = s
                    scaleY = s
                    pivot(424f, 427f)
                },
            )
            // Lengan + tangan mengetuk ponsel.
            Box(
                Modifier.fillMaxSize().graphicsLayer {
                    val m = time.value % 3f
                    val tap = sin(PI.toFloat() * seg(m, 1.0f, 1.3f))
                    translationY = 9f * unit * tap * seg(intro.value, 0.85f, 1f)
                    rotationZ = -1.5f * tap
                    pivot(340f, 280f)
                },
            ) {
                Part(Res.drawable.ill3_arm)
                Part(Res.drawable.ill3_tap_hand)
            }
            Box(Modifier.fillMaxSize().headMotion(time)) {
                Part(Res.drawable.ill1_neck)
                Part(Res.drawable.ill1_face)
                Part(Res.drawable.ill1_hair_front)
                Part(Res.drawable.ill1_eyes, Modifier.blink(time, 263f, 166f))
                Part(
                    Res.drawable.ill3_mouth,
                    Modifier.graphicsLayer {
                        // Tersenyum lebar: mulut membuka sedikit lebih besar saat notifikasi masuk.
                        val m = time.value % 3f
                        scaleY = 1f + 0.18f * sin(PI.toFloat() * seg(m, 1.2f, 2.2f))
                        pivot(271f, 200f)
                    },
                )
            }
        }

        // Riak notifikasi: tiga cincin memuai dan memudar bergantian dari gelembung.
        repeat(3) { k ->
            Part(
                Res.drawable.ill3_ripple,
                Modifier.graphicsLayer {
                    val p = ((time.value / 2f + k / 3f) % 1f)
                    val s = 1f + 0.9f * p
                    scaleX = s
                    scaleY = s
                    alpha = (1f - p) * 0.9f * seg(intro.value, 0.75f, 0.95f)
                    pivot(410f, 200f)
                },
            )
        }
        Part(Res.drawable.ill3_bubble, Modifier.popIn(intro, 0.45f, 0.8f, 380f, 264f))
        Part(
            Res.drawable.ill3_bell,
            Modifier.graphicsLayer {
                val pop = easeOutBack(seg(intro.value, 0.55f, 0.88f)).coerceAtLeast(0f)
                scaleX = pop
                scaleY = pop
                // Lonceng berdering: ayunan teredam setelah ketukan, lalu diam.
                val m = (time.value % 3f) - 1.2f
                rotationZ = if (m in 0f..1.7f) 24f * exp(-2.4f * m) * sin(2f * PI.toFloat() * 2.8f * m) else 0f
                pivot(410f, 177f)
            },
        )
        Part(
            Res.drawable.ill3_badge,
            Modifier.graphicsLayer {
                val pop = easeOutBack(seg(intro.value, 0.7f, 0.95f)).coerceAtLeast(0f)
                val m = (time.value % 3f) - 1.2f
                val beat = if (m in 0f..0.5f) 0.35f * sin(PI.toFloat() * m / 0.5f) else 0f
                scaleX = pop * (1f + beat)
                scaleY = pop * (1f + beat)
                pivot(438f, 178f)
            },
        )
    }
}
